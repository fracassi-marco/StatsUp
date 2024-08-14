import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.relay") version "0.3.12"
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.statsup"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.statsup"
        minSdk = 34
        targetSdk = 34
        versionCode = 40000
        versionName = "4.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
        val properties = Properties()
        if (project.rootProject.file("local.properties").canRead()) {
            properties.load(project.rootProject.file("local.properties").inputStream())
        }

        manifestPlaceholders["MAPS_API_KEY"] = properties.getProperty("maps.apiKey")
        buildConfigField("String", "STRAVA_CLIENT_ID", """"${properties.getProperty("strava.clientId")}"""")
        buildConfigField("String", "STRAVA_CLIENT_SECRET", """"${properties.getProperty("strava.clientSecret")}"""")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.numberpicker)
    implementation("com.github.daikonweb:topinambur:1.9.0")
    // Room
    implementation("androidx.room:room-runtime:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    // Maps
    implementation("com.google.maps.android:maps-compose:2.14.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.maps.android:android-maps-utils:3.5.2")
    // OAuth
    implementation("net.openid:appauth:0.11.1")
    // Xml manipulation
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.1")

    implementation("androidx.compose.material:material-icons-extended")
}