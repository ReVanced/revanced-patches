extension {
    name = "extensions/all/connectivity/wifi/spoof/spoof-wifi.rve"
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
