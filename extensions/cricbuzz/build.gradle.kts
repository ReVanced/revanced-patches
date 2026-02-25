dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:cricbuzz:stub"))
}

android {
    defaultConfig {
        minSdk = 21
    }
}
