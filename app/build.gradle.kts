plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.twobrotherscompany.afinadordecavaquinho"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.twobrotherscompany.afinadordecavaquinho"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation (libs.firebase.analytics)
    implementation (platform(libs.firebase.bom))
    implementation (libs.firebase.firestore)
    implementation(libs.play.services.ads)
    implementation(libs.commons.math3)
}