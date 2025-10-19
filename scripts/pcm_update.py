#!/usr/bin/env python3
"""Generate or validate `.project_context/files.jsonl` incrementally."""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import sys
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Dict, Iterable, Iterator, List, Tuple

ROOT = Path(__file__).resolve().parent.parent
CONTEXT_DIR = ROOT / ".project_context"
MANIFEST_PATH = CONTEXT_DIR / "manifest.json"
FILES_PATH = CONTEXT_DIR / "files.jsonl"
CACHE_PATH = CONTEXT_DIR / ".files_cache.json"
CACHE_VERSION = 1

LANG_BY_SUFFIX: Dict[str, str] = {
    ".kt": "kotlin",
    ".kts": "kotlin",
    ".swift": "swift",
    ".gradle.kts": "kotlin",
    ".xml": "xml",
    ".json": "json",
    ".jsonl": "json",
    ".yaml": "yaml",
    ".yml": "yaml",
    ".md": "markdown",
    ".toml": "toml",
}

MODULES_PATH = CONTEXT_DIR / "modules.jsonl"
GRAPH_PATH = CONTEXT_DIR / "graph.json"
OVERRIDES_PATH = CONTEXT_DIR / "file_overrides.jsonl"
TESTS_PATH = CONTEXT_DIR / "tests.jsonl"

CONFIG_SUFFIXES = {".gradle", ".gradle.kts", ".kts", ".json", ".jsonl", ".yaml", ".yml", ".toml", ".properties"}
ASSET_SUFFIXES = {".png", ".jpg", ".jpeg", ".webp", ".svg"}
VALID_SUFFIXES = {
    ".kt",
    ".kts",
    ".swift",
    ".xml",
    ".json",
    ".jsonl",
    ".yaml",
    ".yml",
    ".gradle",
    ".toml",
    ".md",
}
SPECIAL_FILES = {"gradlew", "gradlew.bat", "Makefile"}


def load_manifest() -> dict:
    if not MANIFEST_PATH.exists():
        raise SystemExit("manifest.json not found; run from repo root")
    with MANIFEST_PATH.open("r", encoding="utf-8") as fh:
        return json.load(fh)


def normalize_ignore(raw: Iterable[str]) -> List[Path]:
    cleaned: List[Path] = []
    for entry in raw:
        entry = entry.rstrip("/")
        if not entry:
            continue
        cleaned.append((ROOT / entry).resolve())
    return cleaned


def is_ignored(path: Path, ignore_dirs: List[Path]) -> bool:
    for ignore in ignore_dirs:
        try:
            path.relative_to(ignore)
            return True
        except ValueError:
            continue
    return False


def is_relative_to(path: Path, other: Path) -> bool:
    try:
        path.relative_to(other)
        return True
    except ValueError:
        return False


def should_index(path: Path) -> bool:
    if path == FILES_PATH or path == CACHE_PATH:
        return False
    if path.name in SPECIAL_FILES:
        return True
    if path.suffix.lower() in VALID_SUFFIXES:
        return True
    if path.name.endswith(".gradle.kts"):
        return True
    rel = path.as_posix()
    if rel.startswith("docs/") or rel.startswith(".project_context/"):
        return True
    return False


def compute_hash(path: Path) -> str:
    digest = hashlib.sha1()
    with path.open("rb") as fh:
        for chunk in iter(lambda: fh.read(1024 * 1024), b""):
            if not chunk:
                break
            digest.update(chunk)
    return f"sha1:{digest.hexdigest()}"


def detect_kind(path: Path) -> str:
    rel = path.as_posix()
    if "src/test" in rel or "src/androidTest" in rel:
        return "test"
    if any(part in {"test", "androidTest"} for part in path.parts):
        return "test"
    suffix = path.suffix.lower()
    if suffix in CONFIG_SUFFIXES or rel.endswith("gradle.kts"):
        return "config"
    if suffix in ASSET_SUFFIXES or "/res/" in rel:
        return "asset"
    if rel.startswith("docs/") or rel.startswith(".project_context/summaries"):
        return "docs"
    if rel.startswith("scripts/"):
        return "script"
    return "source"


def detect_lang(path: Path) -> str:
    if path.name.endswith(".gradle.kts"):
        return "kotlin"
    return LANG_BY_SUFFIX.get(path.suffix.lower(), "unknown")


def iter_files(ignore_dirs: List[Path], include_hidden: bool = False) -> Iterator[Path]:
    for dirpath, dirnames, filenames in os.walk(ROOT):
        current = Path(dirpath)
        if is_ignored(current, ignore_dirs):
            dirnames[:] = []
            continue
        dirnames[:] = [
            d for d in dirnames
            if include_hidden or not d.startswith('.') or is_relative_to(current / d, CONTEXT_DIR)
        ]
        if (
            not include_hidden
            and current != ROOT
            and current.name.startswith('.')
            and not is_relative_to(current, CONTEXT_DIR)
        ):
            continue
        for filename in filenames:
            path = current / filename
            if not include_hidden and filename.startswith('.') and not is_relative_to(path, CONTEXT_DIR):
                continue
            if is_ignored(path, ignore_dirs):
                continue
            if should_index(path):
                yield path


def isoformat(ts: float) -> str:
    return datetime.fromtimestamp(ts, tz=timezone.utc).isoformat().replace("+00:00", "Z")


@dataclass
class CacheEntry:
    size: int
    mtime_ns: int
    record: Dict[str, object]

    @classmethod
    def from_dict(cls, data: Dict[str, object]) -> "CacheEntry":
        return cls(size=data["size"], mtime_ns=data["mtime_ns"], record=data["record"])

    def to_dict(self) -> Dict[str, object]:
        return {"size": self.size, "mtime_ns": self.mtime_ns, "record": self.record}


def load_cache() -> Dict[str, CacheEntry]:
    if not CACHE_PATH.exists():
        return {}
    try:
        raw = json.loads(CACHE_PATH.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return {}
    if raw.get("version") != CACHE_VERSION:
        return {}
    records = raw.get("records", {})
    return {path: CacheEntry.from_dict(entry) for path, entry in records.items()}


def write_cache(cache: Dict[str, CacheEntry]) -> None:
    payload = {
        "version": CACHE_VERSION,
        "records": {path: entry.to_dict() for path, entry in cache.items()},
    }
    CACHE_PATH.parent.mkdir(parents=True, exist_ok=True)
    CACHE_PATH.write_text(json.dumps(payload, ensure_ascii=True, indent=2) + "\n", encoding="utf-8")


def load_overrides() -> Dict[str, Dict[str, object]]:
    if not OVERRIDES_PATH.exists():
        return {}
    overrides: Dict[str, Dict[str, object]] = {}
    with OVERRIDES_PATH.open("r", encoding="utf-8") as fh:
        for line in fh:
            line = line.strip()
            if not line:
                continue
            data = json.loads(line)
            path = data.pop("path")
            overrides[path] = data
    return overrides


def load_modules() -> List[Dict[str, object]]:
    if not MODULES_PATH.exists():
        return []
    modules: List[Dict[str, object]] = []
    with MODULES_PATH.open("r", encoding="utf-8") as fh:
        for line in fh:
            line = line.strip()
            if not line:
                continue
            modules.append(json.loads(line))
    return modules


def build_graph(modules: List[Dict[str, object]]) -> Dict[str, object]:
    if not modules:
        return {"nodes": [], "edges": []}
    node_ids = sorted({module["module"] for module in modules})
    edges = set()
    for module in modules:
        source = module["module"]
        for target in module.get("depends_on", []) or []:
            edges.add((source, target))
    graph = {
        "nodes": [{"id": node_id} for node_id in node_ids],
        "edges": [{"from": frm, "to": to} for frm, to in sorted(edges)],
    }
    return graph


def write_graph(graph: Dict[str, object]) -> None:
    GRAPH_PATH.parent.mkdir(parents=True, exist_ok=True)
    GRAPH_PATH.write_text(json.dumps(graph, ensure_ascii=True, indent=2) + "\n", encoding="utf-8")


def check_graph(modules: List[Dict[str, object]]) -> bool:
    if not modules:
        return True
    expected = build_graph(modules)
    if not GRAPH_PATH.exists():
        print("graph.json missing; run tools/pcm_update.py to refresh", file=sys.stderr)
        return False
    try:
        current = json.loads(GRAPH_PATH.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        print("graph.json invalid JSON; run tools/pcm_update.py to refresh", file=sys.stderr)
        return False
    if current != expected:
        print("graph.json is stale. Run tools/pcm_update.py to refresh.", file=sys.stderr)
        return False
    return True


def build_tests(overrides: Dict[str, Dict[str, object]], records: List[Dict[str, object]]) -> List[Dict[str, object]]:
    if not overrides:
        return []
    metadata: List[Dict[str, object]] = []
    override_paths = {path for path, meta in overrides.items() if meta.get("test_suite") or meta.get("subjects")}
    if not override_paths:
        return []
    record_lookup = {record["path"]: record for record in records}
    for path in sorted(override_paths):
        record = record_lookup.get(path)
        if not record or record.get("kind") != "test":
            continue
        meta = overrides[path]
        entry: Dict[str, object] = {"path": path}
        if "test_suite" in meta:
            entry["suite"] = meta["test_suite"]
        if "subjects" in meta:
            entry["subjects"] = meta["subjects"]
        if "notes" in meta:
            entry["notes"] = meta["notes"]
        metadata.append(entry)
    return metadata


def write_tests(entries: List[Dict[str, object]]) -> None:
    if not entries:
        if TESTS_PATH.exists():
            TESTS_PATH.unlink()
        return
    TESTS_PATH.parent.mkdir(parents=True, exist_ok=True)
    with TESTS_PATH.open("w", encoding="utf-8") as fh:
        for entry in entries:
            fh.write(json.dumps(entry, ensure_ascii=True, separators=(",", ":")))
            fh.write("\n")


def check_tests(overrides: Dict[str, Dict[str, object]], records: List[Dict[str, object]]) -> bool:
    expected = build_tests(overrides, records)
    if not expected:
        return True
    if not TESTS_PATH.exists():
        print("tests.jsonl missing; run tools/pcm_update.py to refresh", file=sys.stderr)
        return False
    current = TESTS_PATH.read_text(encoding="utf-8").splitlines()
    expected_lines = [json.dumps(entry, ensure_ascii=True, separators=(",", ":")) for entry in expected]
    if current != expected_lines:
        print("tests.jsonl is stale. Run tools/pcm_update.py to refresh.", file=sys.stderr)
        return False
    return True


def build_record(path: Path) -> Dict[str, object]:
    stat = path.stat()
    return {
        "path": path.relative_to(ROOT).as_posix(),
        "lang": detect_lang(path),
        "kind": detect_kind(path),
        "size": stat.st_size,
        "hash": compute_hash(path),
        "last_modified": isoformat(stat.st_mtime),
    }


def collect_records(
    files: List[Path],
    cache: Dict[str, CacheEntry],
    overrides: Dict[str, Dict[str, object]],
) -> Tuple[List[Dict[str, object]], Dict[str, CacheEntry]]:
    new_cache: Dict[str, CacheEntry] = {}
    records: List[Dict[str, object]] = []
    for path in files:
        rel = path.relative_to(ROOT).as_posix()
        stat = path.stat()
        cached = cache.get(rel)
        needs_refresh = True
        if cached and cached.size == stat.st_size and cached.mtime_ns == stat.st_mtime_ns:
            needs_refresh = False
        if needs_refresh:
            base_record = build_record(path)
        else:
            base_record = cached.record
        override = overrides.get(rel)
        record = {**base_record, **override} if override else base_record
        if override:
            # Ensure cache holds the base record so removing an override propagates.
            cached_record = base_record
        else:
            cached_record = base_record
        new_cache[rel] = CacheEntry(size=stat.st_size, mtime_ns=stat.st_mtime_ns, record=cached_record)
        records.append(record)
    return records, new_cache


def write_records(records: List[Dict[str, object]]) -> None:
    FILES_PATH.parent.mkdir(parents=True, exist_ok=True)
    with FILES_PATH.open("w", encoding="utf-8") as out:
        for record in records:
            out.write(json.dumps(record, ensure_ascii=True, separators=(",", ":")))
            out.write("\n")


def parse_args(argv: Iterable[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Update .project_context/files.jsonl")
    parser.add_argument(
        "--include-hidden",
        action="store_true",
        help="Index files inside hidden directories",
    )
    parser.add_argument(
        "--check",
        action="store_true",
        help="Exit with non-zero status if files.jsonl is stale",
    )
    return parser.parse_args(list(argv))


def main(argv: Iterable[str]) -> int:
    args = parse_args(argv)
    modules = load_modules()
    overrides = load_overrides()

    if not args.check and modules:
        graph = build_graph(modules)
        write_graph(graph)

    manifest = load_manifest()
    ignore_dirs = normalize_ignore(manifest.get("ignore", []))
    files = sorted(iter_files(ignore_dirs=ignore_dirs, include_hidden=args.include_hidden))

    existing_cache = load_cache()
    records, new_cache = collect_records(files, existing_cache, overrides)

    record_lines = [json.dumps(record, ensure_ascii=True, separators=(",", ":")) for record in records]

    if args.check:
        ok = True
        if not FILES_PATH.exists():
            print("files.jsonl missing; run tools/pcm_update.py to generate", file=sys.stderr)
            ok = False
        else:
            current_lines = FILES_PATH.read_text(encoding="utf-8").splitlines()
            if record_lines != current_lines:
                print("files.jsonl is stale. Run tools/pcm_update.py to refresh.", file=sys.stderr)
                ok = False
        if modules:
            ok = check_graph(modules) and ok
        if overrides:
            ok = check_tests(overrides, records) and ok
        return 0 if ok else 1

    rel_tests = TESTS_PATH.relative_to(ROOT).as_posix()
    if overrides:
        tests = build_tests(overrides, records)
        write_tests(tests)
        if tests and TESTS_PATH.exists():
            refreshed_base = build_record(TESTS_PATH)
            override = overrides.get(rel_tests)
            refreshed = {**refreshed_base, **override} if override else refreshed_base
            updated = False
            for idx, record in enumerate(records):
                if record["path"] == rel_tests:
                    records[idx] = refreshed
                    updated = True
                    break
            if not updated:
                records.append(refreshed)
            stat = TESTS_PATH.stat()
            new_cache[rel_tests] = CacheEntry(size=stat.st_size, mtime_ns=stat.st_mtime_ns, record=refreshed_base)
        else:
            if TESTS_PATH.exists():
                TESTS_PATH.unlink()
            records = [record for record in records if record["path"] != rel_tests]
            new_cache.pop(rel_tests, None)
    else:
        if TESTS_PATH.exists():
            TESTS_PATH.unlink()
        records = [record for record in records if record["path"] != rel_tests]
        new_cache.pop(rel_tests, None)

    write_records(records)
    write_cache(new_cache)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
