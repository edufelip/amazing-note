import org.gradle.api.JavaVersion
import org.gradle.api.tasks.Sync
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

lateinit var iosArm64Target: KotlinNativeTarget
lateinit var iosSimArm64Target: KotlinNativeTarget

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosArm64Target = iosArm64()
    iosSimArm64Target = iosSimulatorArm64()
    val iosX64 = iosX64()

    listOf(iosArm64Target, iosSimArm64Target, iosX64).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.bundles.preview)
            implementation(libs.activity.compose)
            implementation(libs.credentials.core)
            implementation(libs.credentials.play.services)
            implementation(libs.googleid)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlin.coroutines.core)
            implementation(libs.coil3.compose)
            implementation(libs.coil3.network.ktor3)
            implementation(libs.cupertino.adaptive)
            implementation(libs.cupertino.icons.extended)
            implementation(libs.gitlive.firestore)
            implementation(libs.gitlive.auth)
            implementation(libs.gitlive.storage)
            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        iosMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
    }
}

android {
    namespace = "com.edufelip.amazing_note.composeapp"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose {
    resources {
        packageOfResClass = "com.edufelip.shared.resources"
    }
}

val xcodeConfiguration = providers.gradleProperty("CONFIGURATION").orElse(System.getenv("CONFIGURATION") ?: "Debug")
val xcodeSdk = providers.gradleProperty("SDK_NAME").orElse(System.getenv("SDK_NAME") ?: "iphonesimulator")

tasks.register<Sync>("packForXcode") {
    val buildType = xcodeConfiguration.get()
    val sdkName = xcodeSdk.get()
    val target = when {
        sdkName.startsWith("iphoneos", ignoreCase = true) -> iosArm64Target
        else -> iosSimArm64Target
    }
    val framework = target.binaries.getFramework(buildType)
    dependsOn(framework.linkTaskProvider)
    val destinationDir = layout.buildDirectory.dir("xcode-frameworks/$buildType/$sdkName")
    from({ framework.outputDirectory })
    into(destinationDir)
}
