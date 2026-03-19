dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:samsung:radio:stub"))
}

android {
    defaultConfig {
        minSdk = 26
    }
}
