plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.parkirfirebase"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.parkirfirebase"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation ("me.dm7.barcodescanner:zxing:1.9.13'")
    implementation ("com.budiyev.android:code-scanner:2.1.0")
    implementation ("com.google.zxing:core:3.4.1")
    implementation ("com.journeyapps:zxing-android-embedded:4.0.0")
    implementation ("com.google.android.gms:play-services-vision:20.1.3")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-firestore:24.9.1")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("androidx.multidex:multidex:2.0.1")
    implementation ("com.google.zxing:core:3.4.1")
    implementation ("me.dm7.barcodescanner:zxing:1.9.13")
    implementation ("com.google.firebase:firebase-database:20.3.0")
    implementation("com.google.android.material:material:1.10.0")
    implementation("com.etebarian:meow-bottom-navigation-java:1.2.0")
    implementation ("com.google.mlkit:barcode-scanning:17.2.0")
    implementation ("com.google.android.gms:play-services-mlkit-barcode-scanning:18.3.0")
    implementation ("com.github.bumptech.glide:glide:4.14.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.14.2")
    coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:2.0.4")
}