dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:nothingx:stub"))
}

android {
    defaultConfig {
        minSdk = 23
    }
}