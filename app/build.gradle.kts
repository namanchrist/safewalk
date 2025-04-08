plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.safewalk"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.safewalk"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // Use the real Google Maps API key
        manifestPlaceholders["MAPS_API_KEY"] = "your_api_key"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

configurations.all {
    resolutionStrategy {
        // Force all Firebase dependencies to use the same version
        force("com.google.firebase:firebase-common:20.3.3")
        
        // Force Compose versions to be compatible
        force("androidx.compose.animation:animation:1.5.4")
        force("androidx.compose.animation:animation-core:1.5.4")
        force("androidx.compose.foundation:foundation:1.5.4")
        force("androidx.compose.material3:material3:1.1.2")
        force("androidx.compose.runtime:runtime:1.5.4")
        force("androidx.compose.ui:ui:1.5.4")
        
        // Force navigation-compose to be compatible with compose runtime
        force("androidx.navigation:navigation-compose:2.7.5")
        force("androidx.activity:activity-compose:1.8.1")
    }
}

dependencies {
    // Core and UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database)

    // Compose - use a consistent version
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0") // This provides collectAsState for Flow
    
    // Force specific versions of navigation and activity
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    
    // Maps and Location
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("com.google.maps:google-maps-services:2.2.0")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    
    // Image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation(libs.firebase.auth)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
} 
