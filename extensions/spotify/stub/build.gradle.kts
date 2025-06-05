plugins {
    id(libs.plugins.android.library.get().pluginId)
}

android {
    namespace = "app.revanced.extension"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
