extension {
    name = "extensions/all/screencapture/remove-screen-capture-restriction.rve"
}

android {
    namespace = "app.revanced.extension"
    compileSdk = 33

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    compileOnly(libs.annotation)
}
