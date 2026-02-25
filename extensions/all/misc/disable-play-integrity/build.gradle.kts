android {
    defaultConfig {
        minSdk = 21
    }

    buildFeatures {
        aidl = true
    }
}

dependencies {
    compileOnly(libs.annotation)
}
