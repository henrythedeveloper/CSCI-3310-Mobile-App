import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}



android {
    namespace = "com.example.csci_3310_nav"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.csci_3310_nav"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load local.properties
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        // Get the key safely
        val mapsKey = properties.getProperty("MAPS_API_KEY") ?: ""

        // 1. Pass to Java Code (BuildConfig.MAPS_API_KEY)
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsKey\"")

        // 2. Pass to AndroidManifest (${MAPS_API_KEY}) <--- YOU WERE MISSING THIS
        manifestPlaceholders["MAPS_API_KEY"] = mapsKey
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation("com.squareup.okhttp3:okhttp:5.3.0")
    implementation("com.google.maps.android:android-maps-utils:3.19.1")
    implementation("com.google.code.gson:gson:2.13.2")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}