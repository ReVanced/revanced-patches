dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:spotify:stub"))
    compileOnly(libs.annotation)
}

android {
    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
