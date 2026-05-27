import java.net.URL
import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aistudio.radio61.hqymw"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Media3 (ExoPlayer & Session)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)

    // Network & Image
    implementation(libs.okhttp)
    implementation(libs.coil.compose)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

val downloadCoverLogo = tasks.register("downloadCoverLogo") {
    val dest = file("src/main/res/drawable/og.png")
    dest.parentFile.mkdirs()
    doLast {
        try {
            val url = URL("https://mixerfm.com/img/cover.png")
            val connection = url.openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()
            val outputStream = dest.outputStream()
            val buffer = ByteArray(4096)
            var bytesRead = inputStream.read(buffer)
            while (bytesRead != -1) {
                outputStream.write(buffer, 0, bytesRead)
                bytesRead = inputStream.read(buffer)
            }
            outputStream.close()
            inputStream.close()
            logger.lifecycle("Downloaded Mixer FM logo successfully to ${dest.absolutePath}")
        } catch (e: Exception) {
            logger.error("Failed to download Mixer FM logo: $e")
        }
    }
}

tasks.matching { it.name == "preBuild" }.all {
    dependsOn(downloadCoverLogo)
}
