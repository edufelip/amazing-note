plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    id("org.jetbrains.kotlin.native.cocoapods")
    alias(libs.plugins.sqldelight)
}

kotlin {
    // Android target
    @Suppress("UNUSED_VARIABLE")
    androidTarget()

    // iOS targets
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.1.0"
        summary = "Shared KMP module for Amazing Note"
        homepage = "https://example.com/amazing-note"
        ios.deploymentTarget = "14.0"
        // Ensure CocoaPods links sqlite3 when integrating the shared framework
        // Value is injected verbatim into the podspec, so include quotes/array.
        extraSpecAttributes["libraries"] = "['sqlite3']"
        framework {
            // The name your iOS app will import
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.coroutines.core)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.materialIconsExtended)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        androidMain {
            dependencies {
                implementation(libs.sqldelight.android.driver)
            }
        }
        iosMain {
            dependencies {
                implementation(libs.sqldelight.native.driver)
            }
        }
    }
}

// Ensure all Kotlin/Native iOS binaries link with SQLite3
kotlin.targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget::class.java).configureEach {
    binaries.all {
        linkerOpts("-lsqlite3")
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

sqldelight {
    databases {
        create("NoteDatabase") {
            packageName.set("com.edufelip.shared.db")
        }
    }
}
