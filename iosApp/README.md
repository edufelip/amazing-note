# iOS module

The iOS host app consumes the Compose-based Kotlin Multiplatform UI (`ComposeApp.framework`) plus the shared data layer. Swift Package Manager supplies Firebase and other iOS dependencies—no CocoaPods required.

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

2. **Build and sync the Compose framework via Gradle.**  
   Either use the helper script:
   ```bash
   ./scripts/rebuild_ios.sh
   ```
   or invoke the Gradle tasks directly (set `SDK_NAME` to `iphonesimulator` or `iphoneos`):
   ```bash
   ./gradlew -PCONFIGURATION=Debug -PSDK_NAME=iphonesimulator \
             :composeApp:packForXcode \
             :iosApp:packForXcode
   ```
   The task copies `ComposeApp.framework` into `iosApp/Frameworks/<CONFIGURATION>-<platform>/` for Xcode.

3. **Embed the framework in Xcode.**  
   In the **iosApp** target, open *General ▸ Frameworks, Libraries, and Embedded Content*, add `ComposeApp.framework` from `iosApp/Frameworks/<CONFIGURATION>-<platform>/` and set it to **Embed & Sign**. No extra embed script is required.

4. **Run from Xcode or the CLI.**  
   After syncing the framework, regular Xcode builds (or `xcodebuild`) pick up the fresh `ComposeApp.framework`.

### Android Studio

The `iosApp` Gradle module now applies the Kotlin Multiplatform plugin and publishes the usual `runDebugExecutableIosSimulatorArm64` task. The task itself is a no-op, but it allows the Android Studio KMM plug-in to surface an iOS run configuration again alongside the Android one.

## Notes

- Keep `iosApp/iosApp/GoogleService-Info.plist` in place for Firebase configuration.
- Set your signing team in *Signing & Capabilities* if you hit code-signing warnings.
- Simulator images must be installed separately through Xcode ▸ Settings ▸ Platforms.
