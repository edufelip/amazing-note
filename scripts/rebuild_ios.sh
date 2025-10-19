#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

CONFIG=${CONFIG:-Debug}
SCHEME=${SCHEME:-iosApp}
DEST_SIM=${DEST_SIM:-"platform=iOS Simulator,name=iPhone 15"}

chmod +x ./gradlew
xcodebuild -resolvePackageDependencies -project iosApp/iosApp.xcodeproj
./gradlew :shared:embedAndSignAppleFrameworkForXcode \
  -PXCODE_CONFIGURATION="$CONFIG" \
  -PXCODE_SDK=iphonesimulator \
  -PXCODE_DESTINATION="generic/platform=iOS Simulator"

./gradlew :shared:embedAndSignAppleFrameworkForXcode \
  -PXCODE_CONFIGURATION="$CONFIG" \
  -PXCODE_SDK=iphoneos \
  -PXCODE_DESTINATION="generic/platform=iOS"

xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme "$SCHEME" \
  -configuration "$CONFIG" \
  -sdk iphonesimulator \
  -destination "$DEST_SIM" \
  build
