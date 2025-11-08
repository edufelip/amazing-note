plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

fun String.toVersionCode(): Int {
    val numeric = substringBefore("-")
    val segments = numeric.split('.')
    val major = segments.getOrNull(0)?.toIntOrNull() ?: 0
    val minor = segments.getOrNull(1)?.toIntOrNull() ?: 0
    val patch = segments.getOrNull(2)?.toIntOrNull() ?: 0
    return (major * 10000) + (minor * 100) + patch
}

val gitVersionName = rootProject.version.toString()

android {
    namespace = "com.edufelip.amazing_note"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.edufelip.amazing_note"
        minSdk = 30
        targetSdk = 36
        versionName = gitVersionName
        versionCode = gitVersionName.toVersionCode()
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes +=
                setOf(
                    "META-INF/DEPENDENCIES",
                    "META-INF/LICENSE",
                    "META-INF/LICENSE.txt",
                    "META-INF/license.txt",
                    "META-INF/NOTICE",
                    "META-INF/NOTICE.txt",
                    "META-INF/notice.txt",
                    "META-INF/ASL2.0",
                    "META-INF/AL2.0",
                    "META-INF/LGPL2.1",
                    "META-INF/*.kotlin_module",
                )
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    // Coroutines
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.android)

    // Koin
    implementation(libs.koin.android)

    // Local Unit Tests
    testImplementation(libs.junit)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.truth)

    // Firebase Auth (Android) via BoM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.common.ktx)
    implementation(libs.firebase.crashlytics)
    implementation(libs.gitlive.app)
    implementation(libs.gitlive.auth)

    // Google Identity Services (Credential Manager + Google ID)
    implementation(libs.credentials.core)
    implementation(libs.credentials.play.services)
    implementation(libs.googleid)

    // Shared KMP module
    implementation(project(":shared"))
    // Compose Multiplatform UI
    implementation(project(":composeApp"))
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
