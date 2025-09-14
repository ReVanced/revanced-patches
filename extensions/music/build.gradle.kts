dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(libs.annotation)
}

android {
    defaultConfig {
        minSdk = 26
    }
}
