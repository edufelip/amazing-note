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
  SDK_NAME_SHORT="iphoneos"
else
  DEST="generic/platform=iOS Simulator"
  SDK_NAME_SHORT="iphonesimulator"
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

# Build and sync ComposeApp framework
./gradlew \
  -PCONFIGURATION="$CONFIG" \
  -PSDK_NAME="$SDK_NAME_SHORT" \
  :composeApp:packForXcode \
  :iosApp:syncComposeFramework

FRAMEWORK_PATH="iosApp/Frameworks/${CONFIG}-${SDK_NAME_SHORT}/ComposeApp.framework"
if [[ ! -d "$FRAMEWORK_PATH" ]]; then
  echo "ERROR: ComposeApp.framework not found at $FRAMEWORK_PATH"
  exit 1
fi

# Copy result into Xcode build products
rm -rf "$BUILD_DIR/ComposeApp.framework"
rsync -a "$FRAMEWORK_PATH" "$BUILD_DIR/"
echo "Copied ComposeApp.framework to $BUILD_DIR"

# Final Xcode build to produce the app with the fresh framework embedded
xcodebuild -project iosApp/iosApp.xcodeproj \
           -scheme "$SCHEME" \
           -configuration "$CONFIG" \
           -destination "$DEST" \
           CODE_SIGNING_ALLOWED=NO \
           build
