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
PRODUCTS_DIR="$DERIVED_DATA/Build/Products/${CONFIG}-${SDK_NAME_SHORT}"
if [[ ! -d "$PKG_DIR" || -z "$(ls -A "$PKG_DIR" 2>/dev/null)" ]]; then
  xcodebuild -project iosApp/iosApp.xcodeproj \
    -scheme "$SCHEME" \
    -configuration "$CONFIG" \
    -destination "$DEST" \
    -derivedDataPath "$DERIVED_DATA" \
    CODE_SIGNING_ALLOWED=NO \
    build >/dev/null
fi

if [[ -d "$PKG_DIR" && -n "$(ls -A "$PKG_DIR" 2>/dev/null)" ]]; then
  echo "$PKG_DIR"
  exit 0
fi

if [[ -d "$PRODUCTS_DIR" && -n "$(ls -A "$PRODUCTS_DIR" 2>/dev/null)" ]]; then
  echo "$PRODUCTS_DIR"
  exit 0
fi

if [[ ! -d "$PKG_DIR" && ! -d "$PRODUCTS_DIR" ]]; then
  echo "ERROR: PackageFrameworks or Products directory not found under $DERIVED_DATA/Build/Products" >&2
  exit 1
fi
