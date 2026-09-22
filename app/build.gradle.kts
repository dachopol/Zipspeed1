plugins {
    id("com.android.application")
}

android {
    namespace = "com.anakinyoo.testspeed"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.aistudio.zipspeed.zskt"
        minSdk = 24
        targetSdk = 36
        versionCode = 44
        versionName = "44.0.0"

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
