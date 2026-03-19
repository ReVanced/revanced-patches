dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:syncforreddit:stub"))
    compileOnly(libs.annotation)
    compileOnly(libs.okhttp)
}

android {
    defaultConfig {
        minSdk = 23
    }
}
