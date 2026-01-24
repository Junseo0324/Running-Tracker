
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-kapt")
}


android {
    namespace = "com.devhjs.runningtracker"
    compileSdk = 36

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }
    val mapsApiKey = localProperties.getProperty("MAPS_API_KEY") ?: ""
    val admobAppIdReal = localProperties.getProperty("ADMOB_APP_ID") ?: ""
    val admobBannerIdReal = localProperties.getProperty("ADMOB_BANNER_ID") ?: ""
    val admobInterstitialIdReal = localProperties.getProperty("ADMOB_INTERSTITIAL_ID") ?: ""
    
    // Test IDs
    val admobAppIdTest = "ca-app-pub-3940256099942544~3347511713"
    val admobBannerIdTest = "ca-app-pub-3940256099942544/6300978111"
    val admobInterstitialIdTest = "ca-app-pub-3940256099942544/1033173712"

    defaultConfig {
        applicationId = "com.devhjs.runningtracker"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            versionNameSuffix = "-dev"
        }
        create("prod") {
            dimension = "environment"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // Release Configuration: Use Real IDs
            manifestPlaceholders["ADMOB_APP_ID"] = admobAppIdReal
            buildConfigField("String", "ADMOB_BANNER_ID", "\"$admobBannerIdReal\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$admobInterstitialIdReal\"")
        }
        debug {
            // Debug Configuration: Use Test IDs
            manifestPlaceholders["ADMOB_APP_ID"] = admobAppIdTest
            buildConfigField("String", "ADMOB_BANNER_ID", "\"$admobBannerIdTest\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$admobInterstitialIdTest\"")
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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.runtime.livedata)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    
    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Maps & Location & Ads
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.play.services.ads)
    
    // Accompanist Permissions
    // Accompanist Permissions
    implementation(libs.accompanist.permissions)
    
    // Timber
    implementation(libs.timber)
    
    // Serialization
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}