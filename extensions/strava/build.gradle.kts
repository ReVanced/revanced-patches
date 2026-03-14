dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:strava:stub"))
    compileOnly(libs.okhttp)
}

android {
    defaultConfig {
        minSdk = 26
    }
}
