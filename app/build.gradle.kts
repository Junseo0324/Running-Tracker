
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
        targetSdk = 36
        versionCode = 8
        versionName = "1.6.0"

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
             buildConfigField("boolean", "SCREENSHOT_MODE", "false")

        }
        debug {
            // 번역 누락 · RTL 깨짐을 확인할 수 있도록 의사 언어(en-XA, ar-XB)를 켠다.
            // 기기 설정 > 개발자 옵션에서 해당 언어를 고르면 적용된다.
            isPseudoLocalesEnabled = true

            // Debug Configuration: Use Test IDs
            manifestPlaceholders["ADMOB_APP_ID"] = admobAppIdTest
            buildConfigField("String", "ADMOB_BANNER_ID", "\"$admobBannerIdTest\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"$admobInterstitialIdTest\"")
            // 스토어 스크린샷 촬영용: ./gradlew ... -PscreenshotMode=true 로 빌드하면 광고를 숨긴다.
            buildConfigField("boolean", "SCREENSHOT_MODE", (project.findProperty("screenshotMode") == "true").toString())
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
    androidResources {
        // values-xx 폴더를 기준으로 locales_config 를 만들어 Android 13+ 앱별 언어 설정에 노출한다.
        // 기본 언어는 src/main/res/resources.properties 의 unqualifiedResLocale.
        generateLocaleConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    testOptions {
        // android.jar 스텁 메서드가 "Stub!" 예외 대신 기본값을 반환하도록 처리
        unitTests.isReturnDefaultValues = true
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

    // Coil (쿠팡 추천 상품 이미지)
    implementation(libs.coil.compose)
    
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // 메모리 누수 탐지 (debug 빌드에만 포함)
    debugImplementation(libs.leakcanary.android)
}

kapt {
    correctErrorTypes = true
}

ksp {
    // Room 스키마를 JSON 으로 내보내 마이그레이션 안전성을 검증할 수 있게 한다.
    arg("room.schemaLocation", "$projectDir/schemas")
}

// 마이그레이션 테스트(MigrationTestHelper)가 내보낸 스키마를 읽을 수 있도록 androidTest 에셋에 포함한다.
android.sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")