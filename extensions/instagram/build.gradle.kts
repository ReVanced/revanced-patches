dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:instagram:stub"))
}

android {
    defaultConfig {
        minSdk = 26
    }
}
