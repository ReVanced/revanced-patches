plugins {
    id(libs.plugins.android.library.get().pluginId)
}

dependencies {
    compileOnly(libs.annotation)
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
