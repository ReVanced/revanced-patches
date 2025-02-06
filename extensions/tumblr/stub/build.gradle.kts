android.namespace = "app.revanced.extension"

plugins {
    id(libs.plugins.android.library.get().pluginId)
}

android {
    namespace = "app.revanced.extension"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }
}
