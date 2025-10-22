#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd -- "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

CONFIG="${CONFIG:-Debug}"
SCHEME="${SCHEME:-iosApp}"
PLATFORM="${PLATFORM_NAME:-iphonesimulator}" # iphonesimulator | iphoneos

export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
export PATH="$JAVA_HOME/bin:$PATH"
chmod +x ./gradlew

# Determine Gradle target suffix/output directory
if [[ "$PLATFORM" == "iphoneos" ]]; then
  DEST="generic/platform=iOS"
  SUFFIX="IosArm64"
  OUTDIR="iosArm64"
else
  DEST="generic/platform=iOS Simulator"
  if [[ "$(uname -m)" == "arm64" ]]; then
    SUFFIX="IosSimulatorArm64"
    OUTDIR="iosSimulatorArm64"
  else
    SUFFIX="IosX64"
    OUTDIR="iosX64"
  fi
fi

# Ensure SwiftPM frameworks (Firebase, etc.) exist in DerivedData
xcodebuild -project iosApp/iosApp.xcodeproj \
           -scheme "$SCHEME" \
           -configuration "$CONFIG" \
           -destination "$DEST" \
           CODE_SIGNING_ALLOWED=NO \
           build >/dev/null

# Resolve CONFIGURATION_BUILD_DIR and expand '~'
RAW_BUILD_DIR=$(
  xcodebuild -showBuildSettings \
             -project iosApp/iosApp.xcodeproj \
             -scheme "$SCHEME" \
             -configuration "$CONFIG" \
             -destination "$DEST" 2>/dev/null |
  sed -n 's/^[[:space:]]*CONFIGURATION_BUILD_DIR = //p' | tail -n1
)

if [[ -z "$RAW_BUILD_DIR" ]]; then
  echo "ERROR: CONFIGURATION_BUILD_DIR could not be determined."
  exit 1
fi

BUILD_DIR="$(python3 - <<'PY' "$RAW_BUILD_DIR"
import os, sys
print(os.path.abspath(os.path.expanduser(sys.argv[1])))
PY
)"

PKG_DIR="$BUILD_DIR/PackageFrameworks"
if [[ ! -d "$PKG_DIR" ]]; then
  echo "WARNING: $PKG_DIR does not exist. Run an Xcode build first so SwiftPM frameworks are produced."
fi

# Link the Shared framework for this target
./gradlew ":shared:link${CONFIG}Framework${SUFFIX}" \
  -PXCODE_FRAMEWORKS_BUILD_DIR="$PKG_DIR"

FRAMEWORK_PATH="shared/build/bin/${OUTDIR}/$(echo "$CONFIG" | tr '[:upper:]' '[:lower:]')Framework/Shared.framework"
if [[ ! -d "$FRAMEWORK_PATH" ]]; then
  echo "ERROR: Linked framework not found at $FRAMEWORK_PATH"
  exit 1
fi

# Copy result into Xcode build products
rm -rf "$BUILD_DIR/Shared.framework"
rsync -a "$FRAMEWORK_PATH" "$BUILD_DIR/"
echo "Copied Shared.framework to $BUILD_DIR"

# Final Xcode build to produce the app with the fresh framework embedded
xcodebuild -project iosApp/iosApp.xcodeproj \
           -scheme "$SCHEME" \
           -configuration "$CONFIG" \
           -destination "$DEST" \
           CODE_SIGNING_ALLOWED=NO \
           build
