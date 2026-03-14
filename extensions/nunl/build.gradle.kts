dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:nunl:stub"))
}

android {
    defaultConfig {
        minSdk = 26
    }
}
