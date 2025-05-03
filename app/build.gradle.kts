plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.stayeaseapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.stayeaseapp"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // ✅ Compose
    implementation("androidx.compose.ui:ui:1.6.7")
    implementation("androidx.compose.material:material:1.6.7")
    implementation("androidx.compose.material:material-icons-extended:1.6.7") // ✅ Fixes Visibility icons
    implementation("androidx.compose.ui:ui-text:1.6.7")
    implementation("androidx.compose.runtime:runtime-saveable:1.6.0") // ✅ Fixes rememberSaveable
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation ("androidx.compose.material3:material3:1.2.1")

    // ✅ Tooling
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.0")

    // ✅ Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-database-ktx")

    // ✅ Firebase Core Services
    implementation("com.google.firebase:firebase-auth:21.1.0")
    implementation("com.google.firebase:firebase-analytics:21.0.0")
    implementation("com.google.android.gms:play-services-auth:20.1.0")

    // ✅ Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // ✅ Volley
    implementation("com.android.volley:volley:1.2.1")
    implementation(libs.volley)

    // ✅ Android Core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    // ✅ Media3 (if used)
    implementation(libs.androidx.media3.common.ktx)

    // ✅ Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    //Coil for loading Cloudinary images
    implementation("io.coil-kt:coil-compose:2.5.0")

// QR code generation
    implementation("com.google.zxing:core:3.5.2")
    //firebase messaging integration
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation ("com.google.firebase:firebase-inappmessaging-display:20.0.0")
}
