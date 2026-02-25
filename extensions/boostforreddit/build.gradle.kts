dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:boostforreddit:stub"))
    compileOnly(libs.annotation)
    compileOnly(libs.okhttp)
}

android {
    defaultConfig {
        minSdk = 21
    }
}
