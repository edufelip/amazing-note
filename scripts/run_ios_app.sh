#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

PROJECT_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
SCHEME="iosApp"
PROJECT_PATH="$PROJECT_ROOT/iosApp/iosApp.xcodeproj"
DERIVED_DATA="$PROJECT_ROOT/build/derived"

log() {
  printf '\033[1;34m[run-ios]\033[0m %s\n' "$*"
}

die() {
  printf '\033[1;31m[run-ios]\033[0m %s\n' "$*" >&2
  exit 1
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    die "Missing required command: $1"
  fi
}

require_command xcodebuild
require_command xcrun
require_command python3
require_command /usr/libexec/PlistBuddy

DEVICE_JSON=$(mktemp)
xcrun simctl list devices --json > "$DEVICE_JSON"
PHYSICAL_TXT=$(mktemp)
PHY_FORMAT="json"
if xcrun devicectl list devices --json-output "$PHYSICAL_TXT" >/dev/null 2>&1; then
  PHY_FORMAT="json"
else
  PHY_FORMAT="text"
  xcrun xctrace list devices > "$PHYSICAL_TXT" 2>/dev/null || true
fi

echo "Available targets:"

python3 - <<'PY' "$DEVICE_JSON" "$PHYSICAL_TXT" "$PHY_FORMAT"
import json, re, sys
device_json_path, physical_path, physical_format = sys.argv[1:4]
with open(device_json_path) as fh:
    device_data = json.load(fh)["devices"]

sim_entries = []
index = 1
for runtime, devices in sorted(device_data.items()):
    for device in devices:
        if not device.get("isAvailable", True):
            continue
        sim_entries.append((index, device["name"], device["udid"], device.get("state", ""), runtime))
        index += 1

if sim_entries:
    print("\nSimulators:")
    for idx, name, udid, state, runtime in sim_entries:
        runtime_label = runtime.split('.')[-1]
        print(f"  {idx:>3}) {name:<25} {runtime_label:<15} {state:<10} {udid}")

with open(physical_path) as fh:
    raw = fh.read()

device_entries = []
try:
    if physical_format == "json":
        data = json.loads(raw)
    else:
        raise json.JSONDecodeError("", "", 0)
except json.JSONDecodeError:
    pattern = re.compile(r"^\s*(.+?) \((.+?)\) \[(.+?)\]", re.MULTILINE)
    pattern_alt = re.compile(r"^\s*(.+?) \((.+?)\) \(([^)]+)\)$", re.MULTILINE)
    for match in pattern.finditer(raw):
        name, platform, udid = match.groups()
        if "iPhone" not in name and "iPad" not in name:
            continue
        device_entries.append((name, platform, udid))
    for match in pattern_alt.finditer(raw):
        name, platform, udid = match.groups()
        if "iPhone" not in name and "iPad" not in name:
            continue
        device_entries.append((name, platform, udid))
else:
    for device in data.get("result", {}).get("devices", []):
        hw = device.get("hardwareProperties", {})
        props = device.get("deviceProperties", {})
        platform = hw.get("platform", "")
        device_type = hw.get("deviceType", "")
        if "iPhone" not in platform and "iPad" not in platform and "iPhone" not in device_type and "iPad" not in device_type:
            continue
        name = props.get("name") or device.get("name", "Unnamed")
        udid = hw.get("udid") or device.get("identifier")
        if not udid:
            continue
        display_platform = platform or device_type or "iOS"
        device_entries.append((name, display_platform, udid))

if device_entries:
    print("\nConnected devices:")
    for idx, (name, platform, udid) in enumerate(device_entries):
        label = chr(ord('a') + idx)
        print(f"  {label})  {name:<25} {platform:<15} {udid}")

if not sim_entries and not device_entries:
    print("No devices or simulators available.")
PY

read -rp $'\nSelect a target (number for simulator or letter for device): ' selection

TARGET=$(python3 - <<'PY' "$selection" "$DEVICE_JSON" "$PHYSICAL_TXT" "$PHY_FORMAT"
import json, re, string, sys

selection = sys.argv[1]
device_json_path, physical_path, physical_format = sys.argv[2:5]

choice = None
if selection.isdigit():
    idx = int(selection)
    if idx >= 1:
        choice = ("sim", idx)
elif len(selection) == 1 and selection.isalpha():
    idx = string.ascii_lowercase.find(selection.lower())
    if idx >= 0:
        choice = ("device", idx)

if choice is None:
    sys.exit("Selection must be a simulator number or device letter.")

choice_type, choice_index = choice

with open(device_json_path) as fh:
    device_data = json.load(fh)["devices"]

if choice_type == "sim":
    index = 1
    for runtime, devices in sorted(device_data.items()):
        for device in devices:
            if not device.get("isAvailable", True):
                continue
            if index == choice_index:
                print(json.dumps({
                    "type": "sim",
                    "udid": device["udid"],
                    "name": device["name"],
                    "runtime": runtime,
                }))
                sys.exit(0)
            index += 1
    sys.exit("Invalid simulator selection.")

with open(physical_path) as fh:
    raw = fh.read()

def emit_device(name, platform, udid):
    print(json.dumps({
        "type": "device",
        "udid": udid,
        "name": name,
        "platform": platform,
    }))
    sys.exit(0)

device_entries = []
try:
    if physical_format == "json":
        data = json.loads(raw)
    else:
        raise json.JSONDecodeError("", "", 0)
except json.JSONDecodeError:
    pattern = re.compile(r"^\s*(.+?) \((.+?)\) \[(.+?)\]", re.MULTILINE)
    pattern_alt = re.compile(r"^\s*(.+?) \((.+?)\) \(([^)]+)\)$", re.MULTILINE)
    for match in pattern.finditer(raw):
        name, platform, udid = match.groups()
        if "iPhone" not in name and "iPad" not in name:
            continue
        device_entries.append((name, platform, udid))
    for match in pattern_alt.finditer(raw):
        name, platform, udid = match.groups()
        if "iPhone" not in name and "iPad" not in name:
            continue
        device_entries.append((name, platform, udid))
else:
    for device in data.get("result", {}).get("devices", []):
        hw = device.get("hardwareProperties", {})
        props = device.get("deviceProperties", {})
        platform = hw.get("platform", "")
        device_type = hw.get("deviceType", "")
        if "iPhone" not in platform and "iPad" not in platform and "iPhone" not in device_type and "iPad" not in device_type:
            continue
        name = props.get("name") or device.get("name", "Unnamed")
        udid = hw.get("udid") or device.get("identifier")
        if not udid:
            continue
        display_platform = platform or device_type or "iOS"
        device_entries.append((name, display_platform, udid))

if choice_type == "device":
    if 0 <= choice_index < len(device_entries):
        name, platform, udid = device_entries[choice_index]
        emit_device(name, platform, udid)
    sys.exit("Invalid device selection.")

sys.exit("Invalid selection.")
PY)

if [[ -z "$TARGET" ]]; then
  die "Failed to parse selection."
fi

TARGET_TYPE=$(python3 -c 'import json,sys; print(json.loads(sys.stdin.read())["type"])' <<<"$TARGET")
TARGET_UDID=$(python3 -c 'import json,sys; print(json.loads(sys.stdin.read())["udid"])' <<<"$TARGET")
TARGET_NAME=$(python3 -c 'import json,sys; print(json.loads(sys.stdin.read())["name"])' <<<"$TARGET")

if [[ "$TARGET_TYPE" == "sim" ]]; then
  STATE=$(xcrun simctl list devices | awk -v udid="$TARGET_UDID" '$0 ~ udid {print $NF}' | tr -d '()')
  if [[ "$STATE" != "Booted" ]]; then
    log "Booting simulator $TARGET_NAME ($TARGET_UDID)"
    xcrun simctl boot "$TARGET_UDID" >/dev/null
    open -a Simulator >/dev/null 2>&1 || true
    xcrun simctl bootstatus "$TARGET_UDID" -b
  else
    log "Using booted simulator $TARGET_NAME"
    open -a Simulator >/dev/null 2>&1 || true
  fi
else
  if ! xcrun devicectl list devices >/dev/null 2>&1; then
    die "xcrun devicectl is required (Xcode 15+)."
  fi
fi

EXTRA_ARGS=()
if [[ "$TARGET_TYPE" == "device" ]]; then
  log "Preparing real device build for $TARGET_NAME"
  EXTRA_ARGS+=(-allowProvisioningUpdates)
fi

log "Building $SCHEME for $TARGET_NAME"
XCODE_CMD=(
  xcodebuild
  -project "$PROJECT_PATH"
  -scheme "$SCHEME"
  -configuration Debug
  -destination "id=$TARGET_UDID"
  -derivedDataPath "$DERIVED_DATA"
)
if [[ ${#EXTRA_ARGS[@]} -gt 0 ]]; then
  XCODE_CMD+=("${EXTRA_ARGS[@]}")
fi
XCODE_CMD+=(build)
"${XCODE_CMD[@]}" >/dev/null

if [[ "$TARGET_TYPE" == "sim" ]]; then
  APP_PATH="$DERIVED_DATA/Build/Products/Debug-iphonesimulator/iosApp.app"
else
  APP_PATH="$DERIVED_DATA/Build/Products/Debug-iphoneos/iosApp.app"
fi

if [[ ! -d "$APP_PATH" ]]; then
  die "App bundle not found at $APP_PATH"
fi

BUNDLE_ID=$(/usr/libexec/PlistBuddy -c 'Print :CFBundleIdentifier' "$APP_PATH/Info.plist")

if [[ "$TARGET_TYPE" == "sim" ]]; then
  log "Installing app"
  xcrun simctl install "$TARGET_UDID" "$APP_PATH" >/dev/null

  log "Launching $BUNDLE_ID"
  xcrun simctl terminate "$TARGET_UDID" "$BUNDLE_ID" >/dev/null 2>&1 || true
  xcrun simctl launch "$TARGET_UDID" "$BUNDLE_ID"
else
  log "Installing app on device"
  xcrun devicectl device install app --device "$TARGET_UDID" "$APP_PATH"
  log "Launching $BUNDLE_ID"
  xcrun devicectl device process launch --device "$TARGET_UDID" --terminate-existing "$BUNDLE_ID"
fi

log "Done"

rm -f "$DEVICE_JSON" "$PHYSICAL_TXT"
if [[ "$TARGET_TYPE" == "device" ]]; then
  log "Preparing real device build for $TARGET_NAME"
  EXTRA_ARGS+=(-allowProvisioningUpdates)
  EXTRA_ARGS+=(-allowProvisioningDeviceRegistration)
fi
