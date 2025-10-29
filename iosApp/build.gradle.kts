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
                "-linker-option", "-framework", "-linker-option", "Metal",
                "-linker-option", "-framework", "-linker-option", "CoreText",
                "-linker-option", "-framework", "-linker-option", "CoreGraphics"
            )
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
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

tasks.register("runDebugExecutableIosSimulatorArm64") {
    group = "run"
    description = "Stub task so Android Studio can offer a run configuration for the Debug simulator binary."
    doLast {
        logger.lifecycle(
            "To launch the iOS app, open iosApp/iosApp.xcodeproj in Xcode or run:\n" +
                "  xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 15'"
        )
    }
}

tasks.register("runReleaseExecutableIosSimulatorArm64") {
    group = "run"
    description = "Stub task so Android Studio can offer a run configuration for the Release simulator binary."
    doLast {
        logger.lifecycle(
            "To launch the iOS app in Release, use Xcode with the Release configuration or run the corresponding xcodebuild command."
        )
    }
}
