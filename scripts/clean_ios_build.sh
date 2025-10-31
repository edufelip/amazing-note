#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

PROJECT_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
PROJECT_PATH="$PROJECT_ROOT/iosApp/iosApp.xcodeproj"
SCHEME="iosApp"
CONFIGURATION="Debug"
DERIVED_DATA="$PROJECT_ROOT/build/derived"

log() {
  printf '[clean-ios] %s\n' "$*"
}

die() {
  printf '[clean-ios] %s\n' "$*" >&2
  exit 1
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    die "Missing required command: $1"
  fi
}

require_command xcodebuild

if [[ -d "$DERIVED_DATA" ]]; then
  log "Removing derived data at $DERIVED_DATA"
  rm -rf "$DERIVED_DATA"
else
  log "No derived data directory at $DERIVED_DATA"
fi

log "Cleaning Xcode project ($SCHEME, $CONFIGURATION)"
xcodebuild \
  -project "$PROJECT_PATH" \
  -scheme "$SCHEME" \
  -configuration "$CONFIGURATION" \
  clean >/dev/null

log "Done"
