Run the iOS sample with CocoaPods:

1) Generate the dummy framework once (from repo root):

   ./gradlew :shared:generateDummyFramework

2) Install pods (from iosApp folder):

   cd iosApp
   pod install

3) Open the workspace:

   open iosApp.xcworkspace

4) Build & run the iosApp target. It will present the shared Compose UI via MainViewController().

Notes
- Deployment target is iOS 14.0.
- If code signing fails, set your team in Xcode > Target > Signing & Capabilities.
- The Podfile points to ../shared. If you move folders, update the path.

