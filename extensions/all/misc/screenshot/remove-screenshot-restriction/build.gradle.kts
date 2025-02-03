android {
    namespace = "app.revanced.extension"
    compileSdk = 34

    defaultConfig {
        minSdk = 19
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    compileOnly(libs.annotation)
}
