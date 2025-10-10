plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.native.cocoapods")
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
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
        // Firebase Auth for iOS
        pod("FirebaseAuth")
        // Firestore for iOS
        pod("FirebaseFirestore")
        // Google Sign-In for iOS (used by iosMain Kotlin)
        pod("GoogleSignIn")
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
                // Compose Multiplatform Resources
                implementation(compose.components.resources)
                // Coil 3 multiplatform
                implementation(libs.coil3.compose)
                implementation(libs.coil3.network.ktor3)
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
                implementation(compose.preview)
                implementation(compose.uiTooling)
                implementation(compose.components.resources)
                implementation(libs.activity.compose)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.firebase.auth.ktx)
                implementation(libs.firebase.firestore.ktx)
            }
        }
        iosMain {
            dependencies {
                implementation(libs.sqldelight.native.driver)
                implementation(libs.ktor.client.darwin)
                implementation(compose.components.resources)
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

compose {
    resources {
        packageOfResClass = "com.edufelip.shared.resources"
    }
}

// Ensure compose resource accessors are generated before Kotlin compilation for all targets
tasks
    .matching { it.name.startsWith("compile") && it.name.contains("Kotlin") && it.project.path == ":shared" }
    .configureEach {
        dependsOn(tasks.named("generateComposeResClass"))
    }

// Ensure Android target compiles after common metadata so generated accessors are available
tasks
    .matching { it.name == "compileDebugKotlinAndroid" || it.name == "compileReleaseKotlinAndroid" }
    .configureEach {
        dependsOn(tasks.named("compileKotlinMetadata"))
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
