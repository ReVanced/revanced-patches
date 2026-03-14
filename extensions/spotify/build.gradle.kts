dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:spotify:stub"))
    compileOnly(libs.annotation)
}

android {
    defaultConfig {
        minSdk = 24
    }
}
