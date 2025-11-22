import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    // Android target
    @Suppress("UNUSED_VARIABLE")
    androidTarget()

    val iosArm64 = iosArm64()
    val iosSimArm64 = iosSimulatorArm64()
    val iosX64 = iosX64()
    val firebaseIosFrameworksDir: String? = project.findProperty("firebase.ios.frameworks.dir") as String?
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
                "FirebaseFirestore",
                "-framework",
                "FirebaseStorage",
                "-framework",
                "FirebaseCrashlytics",
            )
            firebaseIosFrameworksDir?.let { linkerOpts("-F", it) }
        }
    }

    listOf(iosArm64, iosSimArm64, iosX64).forEach { t ->
        t.binaries.framework {
            baseName = "Shared"
            isStatic = true
            linkerOpts("-lsqlite3")
        }
        t.configureFirebaseLinkerOpts()
        t.compilations.getByName("main") {
            cinterops.create("commonCrypto") {
                defFile(project.file("src/nativeInterop/cinterop/commonCrypto.def"))
                includeDirs(project.file("src/nativeInterop/cinterop"))
            }
        }
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

sqldelight {
    databases {
        create("NoteDatabase") {
            packageName.set("com.edufelip.shared.db")
        }
    }
}
