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
APP_FRAMEWORKS_DIR="$PRODUCTS_DIR/iosApp.app/Frameworks"
FIREBASE_FRAMEWORKS_DIR="$PRODUCTS_DIR/FirebaseFrameworks"
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
  if [[ -d "$PKG_DIR/FirebaseCore.framework" ]]; then
    echo "$PKG_DIR"
    exit 0
  fi
fi

if [[ ! -d "$PRODUCTS_DIR" || -z "$(ls -A "$PRODUCTS_DIR" 2>/dev/null)" ]]; then
  echo "ERROR: PackageFrameworks or Products directory not found under $DERIVED_DATA/Build/Products" >&2
  exit 1
fi

mkdir -p "$FIREBASE_FRAMEWORKS_DIR"

copy_framework() {
  local name="$1"
  for candidate in "$PKG_DIR" "$PRODUCTS_DIR" "$APP_FRAMEWORKS_DIR"; do
    if [[ -d "$candidate/$name.framework" ]]; then
      rm -rf "$FIREBASE_FRAMEWORKS_DIR/$name.framework"
      cp -R "$candidate/$name.framework" "$FIREBASE_FRAMEWORKS_DIR/"
      return 0
    fi
  done
  return 1
}

create_framework_from_objects() {
  local name="$1"
  shift
  local framework_dir="$FIREBASE_FRAMEWORKS_DIR/$name.framework"
  local objects=()

  for obj in "$@"; do
    local obj_path="$PRODUCTS_DIR/$obj"
    if [[ ! -f "$obj_path" ]]; then
      echo "ERROR: Missing object file for $name: $obj_path" >&2
      return 1
    fi
    objects+=("$obj_path")
  done

  rm -rf "$framework_dir"
  mkdir -p "$framework_dir"
  /usr/bin/libtool -static -o "$framework_dir/$name" "${objects[@]}"
  cat > "$framework_dir/Info.plist" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>CFBundleExecutable</key>
  <string>$name</string>
  <key>CFBundleIdentifier</key>
  <string>com.edufelip.firebase.$name</string>
  <key>CFBundleName</key>
  <string>$name</string>
  <key>CFBundlePackageType</key>
  <string>FMWK</string>
</dict>
</plist>
EOF
}

ensure_framework() {
  local name="$1"
  shift
  if copy_framework "$name"; then
    return 0
  fi
  if [[ "$#" -gt 0 ]]; then
    create_framework_from_objects "$name" "$@"
    return 0
  fi
  echo "ERROR: Unable to locate or build framework $name" >&2
  return 1
}

ensure_framework "FirebaseCore" "FirebaseCore.o"
ensure_framework "FirebaseAuth" "FirebaseAuth.o"
ensure_framework "FirebaseAuthInterop" "FirebaseAuthInterop.o"
ensure_framework "FirebaseFirestore" "FirebaseFirestore.o" "FirebaseFirestoreTarget.o" "FirebaseFirestoreInternalWrapper.o"
ensure_framework "FirebaseAppCheckInterop" "FirebaseAppCheckInterop.o"
ensure_framework "FirebaseFirestoreInternal" "FirebaseFirestoreInternalWrapper.o" "FirebaseFirestoreTarget.o"
ensure_framework "FirebaseStorage" "FirebaseStorage.o"
ensure_framework "FirebaseCrashlytics" "FirebaseCrashlytics.o" "FirebaseCrashlyticsSwift.o"
ensure_framework "FirebaseCoreExtension" "FirebaseCoreExtension.o"
ensure_framework "FirebaseCoreInternal" "FirebaseCoreInternal.o"
ensure_framework "absl"
ensure_framework "grpc"
ensure_framework "grpcpp"
ensure_framework "GTMSessionFetcher" "GTMSessionFetcherCore.o"
ensure_framework "GoogleUtilities" \
  "GoogleUtilities-AppDelegateSwizzler.o" \
  "GoogleUtilities-Environment.o" \
  "GoogleUtilities-Logger.o" \
  "GoogleUtilities-Network.o" \
  "GoogleUtilities-NSData.o" \
  "GoogleUtilities-Reachability.o" \
  "GoogleUtilities-UserDefaults.o" \
  "third-party-IsAppEncrypted.o"
ensure_framework "leveldb" "leveldb.o"
ensure_framework "nanopb" "nanopb.o"
ensure_framework "openssl_grpc"
ensure_framework "RecaptchaInterop" "RecaptchaInterop.o"

echo "$FIREBASE_FRAMEWORKS_DIR"
