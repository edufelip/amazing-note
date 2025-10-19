# iOS module

The iOS target uses Swift Package Manager and the Gradle `embedAndSignAppleFrameworkForXcode` task to consume the shared Kotlin framework. CocoaPods is no longer required.

## Quick start

From the repository root:

```bash
./scripts/rebuild_ios.sh
```

This script builds the shared framework for simulator + device slices and then runs `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp`.

After that, open the project directly:

```bash
open iosApp/iosApp.xcodeproj
```

Select the **iosApp** scheme and run it on your simulator or device. The Xcode build phase named “Embed Shared Framework” calls the Gradle embed task automatically, so future builds only need the standard Xcode Run.

## Notes

- Keep `iosApp/iosApp/GoogleService-Info.plist` in place for Firebase configuration.
- Set your signing team in *Signing & Capabilities* if you hit code-signing warnings.
- Simulator images must be installed separately through Xcode ▸ Settings ▸ Platforms.
