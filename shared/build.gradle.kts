import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kover)
}

kotlin {
    // Android target
    @Suppress("UNUSED_VARIABLE")
    androidTarget()

    val iosArm64 = iosArm64()
    val iosSimArm64 = iosSimulatorArm64()
    val iosX64 = iosX64()
    val firebaseIosFrameworksDir: String? =
        (project.findProperty("firebase.ios.frameworks.dir") as String?)
            ?: System.getenv("FIREBASE_IOS_FRAMEWORKS_DIR")
    val developerDir = System.getenv("DEVELOPER_DIR") ?: "/Applications/Xcode.app/Contents/Developer"
    val stubFrameworksDir = project.file("src/nativeInterop/iosStubs").absolutePath
    if (firebaseIosFrameworksDir == null) {
        logger.warn("firebase.ios.frameworks.dir is not set; relying on Xcode toolchain search paths for Firebase frameworks.")
    }

    fun KotlinNativeTarget.configureFirebaseLinkerOpts() {
        binaries.all {
            linkerOpts(
                "-framework",
                "FirebaseCore",
                "-framework",
                "FirebaseAuth",
                "-framework",
                "FirebaseAuthInterop",
                "-framework",
                "FirebaseFirestore",
                "-framework",
                "FirebaseAppCheckInterop",
                "-framework",
                "FirebaseFirestoreInternal",
                "-framework",
                "FirebaseStorage",
                "-framework",
                "FirebaseCrashlytics",
                "-framework",
                "FirebaseCoreExtension",
                "-framework",
                "FirebaseCoreInternal",
                "-framework",
                "absl",
                "-framework",
                "grpc",
                "-framework",
                "grpcpp",
                "-framework",
                "GTMSessionFetcher",
                "-framework",
                "GoogleUtilities",
                "-framework",
                "leveldb",
                "-framework",
                "nanopb",
                "-framework",
                "openssl_grpc",
                "-framework",
                "RecaptchaInterop",
            )
            firebaseIosFrameworksDir?.let { linkerOpts("-F", it) }
        }
    }

    fun KotlinNativeTarget.configureSwiftCompatibilityLinkerOpts() {
        val platformName =
            when (konanTarget) {
                KonanTarget.IOS_ARM64 -> "iPhoneOS"
                else -> "iPhoneSimulator"
            }
        val sdkDir = "$developerDir/Platforms/$platformName.platform/Developer/SDKs/$platformName.sdk"
        val swiftLibDir = "$developerDir/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/${platformName.lowercase()}"
        val frameworksDir = "$sdkDir/System/Library/Frameworks"
        binaries.all {
            linkerOpts(
                "-F",
                stubFrameworksDir,
                "-F",
                frameworksDir,
                "-syslibroot",
                sdkDir,
                "-L",
                swiftLibDir,
                "-lswiftCompatibility56",
                "-lswiftCompatibilityPacks",
            )
        }
    }

    listOf(iosArm64, iosSimArm64, iosX64).forEach { t ->
        t.binaries.framework {
            baseName = "Shared"
            isStatic = true
            linkerOpts("-lsqlite3")
        }
        t.configureFirebaseLinkerOpts()
        t.configureSwiftCompatibilityLinkerOpts()
    }

    tasks.register("printFrameworkPaths") {
        doLast {
            kotlin.targets.withType(KotlinNativeTarget::class.java).configureEach {
                binaries.withType(Framework::class.java).configureEach {
                    println("${target.name}:$buildType:${outputFile.absolutePath}")
                }
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.coroutines.core)
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.gitlive.firestore)
                implementation(libs.gitlive.auth)
                implementation(libs.gitlive.storage)
                implementation(libs.gitlive.crashlytics)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlin.coroutines.test)
            }
        }
        androidMain {
            dependencies {
                implementation(libs.firebase.auth.ktx)
                implementation(libs.firebase.common.ktx)
                implementation(libs.firebase.crashlytics)
                implementation(libs.sqldelight.android.driver)
                implementation(libs.android.security.crypto)
            }
        }
        iosMain {
            dependencies {
                implementation(libs.sqldelight.native.driver)
            }
        }
    }

    targets.all {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }
}

android {
    namespace = "com.edufelip.amazing_note.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 30
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.withType<KotlinNativeTest>().configureEach {
    val developerDir = System.getenv("DEVELOPER_DIR") ?: "/Applications/Xcode.app/Contents/Developer"
    val swiftRuntimeDir = "$developerDir/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift-5.5/iphonesimulator"
    val swiftCompatDir = "$developerDir/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/iphonesimulator"
    val swiftPaths = listOf(swiftRuntimeDir, swiftCompatDir).joinToString(":")
    environment("DYLD_LIBRARY_PATH", swiftPaths)
    environment("DYLD_FALLBACK_LIBRARY_PATH", swiftPaths)
    environment("SIMCTL_CHILD_DYLD_LIBRARY_PATH", swiftPaths)
    environment("SIMCTL_CHILD_DYLD_FALLBACK_LIBRARY_PATH", swiftPaths)
}

sqldelight {
    databases {
        create("NoteDatabase") {
            packageName.set("com.edufelip.shared.db")
        }
    }
}
