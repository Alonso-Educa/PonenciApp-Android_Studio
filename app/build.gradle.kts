import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties()
localProperties.load(FileInputStream(rootProject.file("local.properties")))

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.ponenciapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.ponenciapp"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Api key de groq
            buildConfigField("String", "GROQ_API_KEY", "\"${localProperties["GROQ_API_KEY"]}\"")

            // Api keys de Cloudinary
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${localProperties["CLOUDINARY_CLOUD_NAME"]}\"")
            buildConfigField("String", "CLOUDINARY_API_KEY", "\"${localProperties["CLOUDINARY_API_KEY"]}\"")
            buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${localProperties["CLOUDINARY_API_SECRET"]}\"")
        }
        release {
            // Api key de groq
            buildConfigField("String", "GROQ_API_KEY", "\"${localProperties["GROQ_API_KEY"]}\"")

            // Api keys de Cloudinary
            buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${localProperties["CLOUDINARY_CLOUD_NAME"]}\"")
            buildConfigField("String", "CLOUDINARY_API_KEY", "\"${localProperties["CLOUDINARY_API_KEY"]}\"")
            buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${localProperties["CLOUDINARY_API_SECRET"]}\"")

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
        buildConfig = true
    }
}

dependencies {

    // ---------------- COMPOSE ----------------
    implementation(platform("androidx.compose:compose-bom:2025.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.9.5")
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.animation)

    // Iconos / Imágenes
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.composables:icons-lucide:1.0.0")
    implementation("io.coil-kt:coil-compose:2.7.0") // AsyncImages

    // Tema
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Accompanist
    implementation("com.google.accompanist:accompanist-permissions:0.31.1-alpha")

    // Para las conversaciones
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Para abrir la cámara
    implementation("androidx.camera:camera-core:1.3.1")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    implementation("com.google.guava:guava:32.0.1-android")

    // Para leer QR
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // Generar QR
    implementation("com.google.zxing:core:3.5.2")

    // Generar documentos
    implementation("org.apache.poi:poi:5.2.0")
    implementation("org.apache.poi:poi-ooxml:5.2.0")

    // Login con google
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // CLOUDINARY (Para subir imágenes)
    implementation("com.cloudinary:cloudinary-android:2.3.1")

    // ---------------- ROOM ----------------
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation(libs.androidx.ui)
    ksp("androidx.room:room-compiler:2.6.1")

    // ---------------- FIREBASE  ----------------
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-common-ktx")
    implementation("com.google.firebase:firebase-auth")
    implementation(libs.firebase.firestore)
    
    // ---------------- CORE ----------------
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // ---------------- TEST ----------------
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}