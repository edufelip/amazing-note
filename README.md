<h1 align="center">Amazing Note</h1>

<p align="center">
  <a href="https://android-arsenal.com/api?level=24"><img alt="API" src="https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://github.com/edufelip"><img alt="Build Status" src="https://img.shields.io/static/v1?label=Android%20CI&message=passing&color=green&logo=android"/></a>
  <a href="https://medium.com/@eduardofelipi"><img alt="Medium" src="https://img.shields.io/static/v1?label=Medium&message=@edu_santos&color=gray&logo=medium"/></a> <br>
  <a href="https://www.youtube.com/channel/UCYcwwX7nDU_U0FP-TsXMwVg"><img alt="Profile" src="https://img.shields.io/static/v1?label=Youtube&message=edu_santos&color=red&logo=youtube"/></a> 
  <a href="https://github.com/edufelip"><img alt="Profile" src="https://img.shields.io/static/v1?label=Github&message=edufelip&color=white&logo=github"/></a> 
  <a href="https://www.linkedin.com/in/eduardo-felipe-dev/"><img alt="Linkedin" src="https://img.shields.io/static/v1?label=Linkedin&message=edu_santos&color=blue&logo=linkedin"/></a> 
</p>

<p align="center">  
🗡️ Amazing Note is a note-taking app designed to collect and organize text!
</p>

<p align="center">
<img src="https://github.com/edufelip/amazing-note/assets/34727187/d516a2f5-3e2c-43ec-b7f8-c69a3a91d079"/>
</p>

## Download
Go to [Google Play](https://play.google.com/store/apps/details?id=com.edufelip.amazing_note) to download the latest App version.

## This project uses
* Compose Multiplatform UI (shared Android/iOS), Material 3 (Material You)
* SQLDelight (shared persistence)
* Kotlin Coroutines
* Dagger Hilt (Android DI)
* Firebase Authentication (Android/iOS)
* Google Identity Services (Android sign-in via Credential Manager + Google ID)
* JUnit and Mockito for unit tests

## Installation
Clone this repository and import into **Android Studio**
```bash
git clone https://github.com/edufelip/amazing-note.git
```
or

```bash
git clone git@github.com:edufelip/amazing-note.git
```

## Build & Run

Android
- Requirements: JDK 17, Android SDK
- Commands:
  - Build debug APK: `./gradlew :app:assembleDebug`
  - Install on device: `./gradlew :app:installDebug`
  - Unit tests: `./gradlew :app:testDebugUnitTest`
  - Lint: `./gradlew :app:lintDebug`
 - Gradle Wrapper: 9.0-milestone-1

iOS (CocoaPods + Compose Multiplatform)
- Requirements: Xcode, CocoaPods, JDK 17
- Bootstrap (generates KMP framework + installs Pods):
  - `chmod +x scripts/ios_bootstrap.sh && ./scripts/ios_bootstrap.sh`
- Open workspace in Xcode:
  - `open iosApp/iosApp.xcworkspace`

Notes
- iOS integrates FirebaseAuth + GoogleSignIn via CocoaPods (configured in shared/build.gradle.kts cocoapods block).
- Ensure `GoogleService-Info.plist` is present in `iosApp/iosApp/` and your URL scheme (REVERSED_CLIENT_ID) is set in `Info.plist`.

## CI Hints
Minimal CI steps you can copy into your pipeline:

- Android (GitHub Actions job snippet)
```
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        distribution: temurin
        java-version: '17'
    - name: Build Debug APK
      run: ./gradlew :app:assembleDebug --stacktrace
    - name: Spotless Check (format enforcement)
      run: ./gradlew spotlessCheck --stacktrace
    - name: Verify Localization
      run: ./gradlew verifyL10n --stacktrace
```

- iOS (macOS runner) – CocoaPods + KMP
```
    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        distribution: temurin
        java-version: '17'
    - name: Bootstrap iOS (Pods + KMP framework)
      run: |
        chmod +x scripts/ios_bootstrap.sh
        ./scripts/ios_bootstrap.sh
    - name: Spotless Check (format enforcement)
      run: ./gradlew spotlessCheck --stacktrace
    - name: Verify Localization
      run: ./gradlew verifyL10n --stacktrace
```

## CI Aggregator
- A convenience task `ci` runs formatting check, build, tests, lint, and l10n verification:
  - `./gradlew ci`

## Formatting
- The project uses Spotless + ktlint for formatting and import optimization (IDE equivalents of Cmd+Opt+L and Ctrl+Alt+O):
  - Apply formatting: `./gradlew spotlessApply`
  - Check formatting only: `./gradlew spotlessCheck`
  - CI runs `spotlessCheck` as part of the `ci` task.

## Android Previews
- Android previews need a strings provider. Use the helper wrapper:
  - `PreviewLocalized { /* your composable */ }`
  - See: `shared/src/androidMain/kotlin/com/edufelip/shared/ui/preview/PreviewWrappers.kt`

## Android DI (Hilt + SQLDelight)

Architecture (Clean): UI → ViewModel → UseCases → Repository → Data Source

- UI (shared Compose in `shared/ui`) now consumes a platform ViewModel via a shared interface `com.edufelip.shared.presentation.NoteUiViewModel`.
- ViewModel (Android: `KmpNoteViewModel`) depends on `NoteUseCases`.
- UseCases depend on the domain `NoteRepository`.
- Data layer (`SqlDelightNoteRepository`) implements the domain repository and talks to SQLDelight.

The Android app injects `NoteUseCases` and exposes `KmpNoteViewModel` to the shared UI, keeping the layering intact.

Authentication on Android uses FirebaseAuth and Google Identity Services:
- Credential Manager + Google ID retrieves an ID token which is exchanged with FirebaseAuth.

- Provider: see `app/src/main/java/com/edufelip/amazing_note/di/AppModule.kt`
- Usage example in an Activity:

```kotlin
@AndroidEntryPoint
class SomeActivity : ComponentActivity() {
    // Android ViewModel exposed to shared UI
    private val vm: KmpNoteViewModel by viewModels()

    setContent {
        ProvideAndroidStrings {
            AmazingNoteApp(
                viewModel = vm,
                // FirebaseAuth service is provided in MainActivity
                onRequestGoogleSignIn = { /* delegated to MainActivity */ }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { /* see above */ }
    }
}
```

MainActivity follows this pattern and is the app launcher.

## Material You & UX
- Dynamic color-aware theme on Android and iOS.
- Sticky section headers grouped by date (Today, This week, This month, Earlier).
- Search + priority FilterChips (All/High/Medium/Low).
- Toggle between Created vs. Updated for grouping and note timestamps.
- Persisted UI state:
  - Dark theme toggle
  - Priority filter selection
  - Date mode (Created vs. Updated)

## Localization
- Shared UI uses a common string key set (`shared/i18n/Strings.kt`) and CompositionLocal `LocalStrings`.
- Providers require platform-native resources by `Str.serialized` key (no shared fallback):
  - Android: `AndroidStrings` looks up `R.string.<key>` dynamically via resource name and formats with `getString(resId, *args)`. Missing keys throw to catch gaps early.
  - iOS: `IosStrings` uses `NSBundle.mainBundle.localizedStringForKey(key)` with basic `%d`/%@ formatting. Missing keys throw to catch gaps early.
- Android string resources live in `shared/src/androidMain/res/values*`.
- iOS localizations are stored under:

  - `iosApp/amazingnote/en.lproj/Localizable.strings`
  - `iosApp/amazingnote/es.lproj/Localizable.strings`
  - `iosApp/amazingnote/pt-BR.lproj/Localizable.strings`

Usage:
- Android `KmpActivity`: wrap content with `ProvideAndroidStrings { ... }`.
- iOS `MainViewController`: wrap content with `ProvideIosStrings { ... }`.

New groups and relative time strings added for headers and time labels. If you localize iOS, add keys:
- `today`, `this_week`, `this_month`, `earlier`
- `updated`, `created`, `updated_just_now`, `updated_minutes_ago`, `updated_hours_ago`, `updated_days_ago`, `created_just_now`, `created_minutes_ago`, `created_hours_ago`, `created_days_ago`

## Enum String Serialization
- Enums that need stable string representations implement `shared/util/StringEnum.kt`.
- Example: `Priority` overrides `toString()` to return `high|medium|low` and exposes `Priority.fromString("high")` for parsing.
- Use `enum.toString()` for persistence and `EnumType.fromString(value)` when reading back to avoid hardcoded strings scattered across the codebase.

## Layouts
<br>
  <p align="left">
            <img alt="splash screen"
            src="https://lh3.googleusercontent.com/b7ctcDFzKJfA_LxgHtLwRb4FZQ3OhxSTyQlTMpVgMHGI1pUFCcMNnQ19ti4mqXFmWOyw" width="24%" 
            title="splash screen">
            <img alt="main screen"
            src="https://lh3.googleusercontent.com/Wlw4_STM8eb8LvNW74dbHWeTcta7YspbchS6j6cZyrLwiremq45tTvut0q0Mx7z1nqQ" width="24%" 
            title="main screen">
            <img alt="main screen dark"
            src="https://lh3.googleusercontent.com/i8kvks2BcrO8dmY8MWkKZ9qogpOLHS0emmluOiYMcrht_rcg6rln136BjN031j_Pkh8" width="24%" 
            title="main screen dark">
            <img alt="main screen dark"
            src="https://lh3.googleusercontent.com/Gvxgct_SqL06tJhaYdq5sphN9uT29s8fjOFyl25eJbTqzVU8jY9_SxCK1G_M0dgQ8g" width="24%" 
            title="main screen dark">

  
## Generating APK
From Android Studio:
1. ***Build*** menu
2. Generate ***Bundle(s) / APK(s)***
3. Build ***APK(s)***
4. Wait for Android studio to build the APK

## Maintainers
This project is mantained by:
* [Eduardo Felipe](http://github.com/edufelip)

## Contributing

1. Fork it
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -m 'Add some feature')
4. Push your branch (git push origin my-new-feature)
5. Create a new Pull Request
