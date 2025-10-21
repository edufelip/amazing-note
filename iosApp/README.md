# iOS module

The iOS host app consumes the shared Kotlin Multiplatform framework. Swift Package Manager supplies Firebase and other iOS dependencies—no CocoaPods required.

## Quick start

1. **Build Swift Package dependencies once.**  
   Open the project in Xcode or run:
   ```bash
   xcodebuild -project iosApp/iosApp.xcodeproj \
              -scheme iosApp \
              -configuration Debug \
              -sdk iphoneos \
              build
   ```
   This compiles Firebase (and other SPM packages) into Xcode’s DerivedData folder so Gradle can link against them.

2. **Link the shared framework via Gradle.**  
   Either use the helper script:
   ```bash
   ./scripts/rebuild_ios.sh
   ```
   or invoke the link tasks directly:
   ```bash
   ./gradlew :shared:linkDebugFrameworkIosArm64 \
             -PXCODE_FRAMEWORKS_BUILD_DIR=/absolute/path/to/DerivedData/.../Build/Products/Debug-iphoneos

   ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 \
             -PXCODE_FRAMEWORKS_BUILD_DIR=/absolute/path/to/DerivedData/.../Build/Products/Debug-iphonesimulator
   ```
   (Use `xcodebuild -showBuildSettings` to locate the `CONFIGURATION_BUILD_DIR` for your scheme/config.)

3. **Embed the framework in Xcode.**  
   In the **iosApp** target, open *General ▸ Frameworks, Libraries, and Embedded Content*, add `Shared.framework` from the build products, and set it to **Embed & Sign**. That removes the need for any extra embed shell script.

4. **Run from Xcode or the CLI.**  
   After linking and embedding, regular Xcode builds (or `xcodebuild`) will pick up the fresh `Shared.framework`.

## Notes

- Keep `iosApp/iosApp/GoogleService-Info.plist` in place for Firebase configuration.
- Set your signing team in *Signing & Capabilities* if you hit code-signing warnings.
- Simulator images must be installed separately through Xcode ▸ Settings ▸ Platforms.
