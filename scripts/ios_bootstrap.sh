#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "[1/3] Ensuring JDK 17 and Gradle wrapper available..."
if ! command -v java >/dev/null 2>&1; then
  echo "Java not found on PATH. Please install JDK 17 and re-run." >&2
  exit 1
fi

echo "[2/3] Generating KMP dummy framework for CocoaPods..."
./gradlew :shared:generateDummyFramework

echo "[3/3] Running CocoaPods install in iosApp..."
cd iosApp
if ! command -v pod >/dev/null 2>&1; then
  echo "CocoaPods not found. Install with: sudo gem install cocoapods" >&2
  exit 1
fi

# Normalize Xcode project for older CocoaPods/xcodeproj gems
if [ -f "iosApp.xcodeproj/project.pbxproj" ]; then
  sed -i '' 's/objectVersion = 77;/objectVersion = 60;/' iosApp.xcodeproj/project.pbxproj || true
  # Remove new attributes CocoaPods 1.13/xcodeproj <1.24 don't understand
  sed -i '' '/minimizedProjectReferenceProxies/d' iosApp.xcodeproj/project.pbxproj || true
  sed -i '' '/preferredProjectObjectVersion/d' iosApp.xcodeproj/project.pbxproj || true
fi

pod install

echo "Done. Open iosApp/iosApp.xcworkspace in Xcode."
