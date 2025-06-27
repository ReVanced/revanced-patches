android.namespace = "app.revanced.extension"

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "app.revanced.extension"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }
}
