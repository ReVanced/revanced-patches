dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:twitch:stub"))
    compileOnly(libs.okhttp)
    compileOnly(libs.retrofit)
    compileOnly(libs.annotation)
    compileOnly(libs.appcompat)
}

android {
    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
