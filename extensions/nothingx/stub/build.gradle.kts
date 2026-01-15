plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "app.revanced.extension"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}