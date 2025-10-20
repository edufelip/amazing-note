import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    // Android target
    @Suppress("UNUSED_VARIABLE")
    androidTarget()

    val iosArm64 = iosArm64()
    val iosSimArm64 = iosSimulatorArm64()

    listOf(iosArm64, iosSimArm64).forEach { t ->
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
                    println("${target.name}:${buildType}:${outputFile.absolutePath}")
                }
            }
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
                implementation(compose.components.resources)
                implementation(libs.coil3.compose)
                implementation(libs.coil3.network.ktor3)
                implementation(compose.materialIconsExtended)
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
                implementation(compose.preview)
                implementation(compose.uiTooling)
                implementation(compose.components.resources)
                implementation(libs.activity.compose)
                implementation(libs.credentials.core)
                implementation(libs.credentials.play.services)
                implementation(libs.googleid)
                implementation(libs.ktor.client.okhttp)
                api(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.auth)
                implementation(libs.firebase.firestore)
                implementation(libs.firebase.storage)
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
