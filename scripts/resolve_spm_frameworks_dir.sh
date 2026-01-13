#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd -- "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

CONFIG="${CONFIG:-Debug}"
SCHEME="${SCHEME:-iosApp}"
PLATFORM="${PLATFORM_NAME:-iphonesimulator}"
DERIVED_DATA="${DERIVED_DATA:-$ROOT_DIR/iosApp/build/DerivedData}"

if [[ "$PLATFORM" == "iphoneos" ]]; then
  DEST="generic/platform=iOS"
  SDK_NAME_SHORT="iphoneos"
else
  DEST="generic/platform=iOS Simulator"
  SDK_NAME_SHORT="iphonesimulator"
fi

PKG_DIR="$DERIVED_DATA/Build/Products/${CONFIG}-${SDK_NAME_SHORT}/PackageFrameworks"
if [[ ! -d "$PKG_DIR" ]]; then
  xcodebuild -project iosApp/iosApp.xcodeproj \
    -scheme "$SCHEME" \
    -configuration "$CONFIG" \
    -destination "$DEST" \
    -derivedDataPath "$DERIVED_DATA" \
    CODE_SIGNING_ALLOWED=NO \
    build >/dev/null
fi

if [[ ! -d "$PKG_DIR" ]]; then
  echo "ERROR: PackageFrameworks not found at $PKG_DIR" >&2
  exit 1
fi

echo "$PKG_DIR"
