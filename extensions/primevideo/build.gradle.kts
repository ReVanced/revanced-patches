dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:primevideo:stub"))
}

android {
    defaultConfig {
        minSdk = 21
    }
}
