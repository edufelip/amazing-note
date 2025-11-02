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

    listOf(iosArm64, iosSimArm64, iosX64).forEach { t ->
        t.binaries.framework {
            baseName = "Shared"
            isStatic = true
            linkerOpts("-lsqlite3")
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
