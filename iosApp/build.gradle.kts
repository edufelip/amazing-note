import org.gradle.api.tasks.Sync
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    applyDefaultHierarchyTemplate()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    targets.withType(KotlinNativeTarget::class.java).configureEach {
        binaries.executable {
            entryPoint = "com.edufelip.iosapp.main"
            freeCompilerArgs += listOf(
                "-linker-option", "-framework",
                "-linker-option", "Metal",
                "-linker-option", "-framework",
                "-linker-option", "CoreText",
                "-linker-option", "-framework",
                "-linker-option", "CoreGraphics",
            )
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":composeApp"))
            }
        }
        val iosMain by getting {
            dependencies {
                implementation(compose.animation)
                implementation(compose.materialIconsExtended)
            }
        }
    }
}

// Lightweight run stubs so Android Studio exposes iOS run configurations.
tasks.register("runDebugExecutableIosSimulatorArm64") {
    group = "run"
    description = "Stub task: build via Gradle, then launch from Xcode or scripts/run_ios_app.sh."
    dependsOn("linkDebugExecutableIosSimulatorArm64")
    doLast {
        logger.lifecycle("Debug executable linked: run from Xcode or ./scripts/run_ios_app.sh")
    }
}

tasks.register("runReleaseExecutableIosSimulatorArm64") {
    group = "run"
    description = "Stub task for Release simulator builds."
    dependsOn("linkReleaseExecutableIosSimulatorArm64")
    doLast {
        logger.lifecycle("Release executable linked: run from Xcode if needed.")
    }
}

val configuration = providers.gradleProperty("CONFIGURATION").orElse(System.getenv("CONFIGURATION") ?: "Debug")
val sdkName = providers.gradleProperty("SDK_NAME").orElse(System.getenv("SDK_NAME") ?: "iphonesimulator")

val composeAppProject = project(":composeApp")
val frameworksOutput = layout.projectDirectory.dir("Frameworks")

fun composeFrameworkDir(config: String, sdk: String) =
    composeAppProject.layout.buildDirectory.dir("xcode-frameworks/$config/$sdk")

tasks.register<Sync>("syncComposeFramework") {
    val buildType = configuration.get()
    val sdk = sdkName.get()
    val sdkSegment = if (sdk.startsWith("iphoneos", ignoreCase = true)) "iphoneos" else "iphonesimulator"
    description = "Copy ComposeApp.framework for $buildType ($sdk) into iosApp/Frameworks."
    group = "build"
    dependsOn(":composeApp:packForXcode")
    from(composeFrameworkDir(buildType, sdk))
    into(frameworksOutput.dir("$buildType-$sdkSegment").asFile)
}

tasks.register("packForXcode") {
    group = "build"
    description = "Prepare ComposeApp.framework for Xcode by syncing it into iosApp/Frameworks."
    dependsOn("syncComposeFramework")
    doLast {
        val buildType = configuration.get()
        val sdk = sdkName.get()
        val sdkSegment = if (sdk.startsWith("iphoneos", ignoreCase = true)) "iphoneos" else "iphonesimulator"
        logger.lifecycle("Framework synced to ${frameworksOutput.dir("$buildType-$sdkSegment").asFile}")
    }
}
