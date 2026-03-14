dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(libs.annotation)
    compileOnly(libs.okhttp)
}

android {
    defaultConfig {
        minSdk = 22
    }
}
