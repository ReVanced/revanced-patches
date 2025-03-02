dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:youtube:stub"))
    compileOnly(libs.annotation)
}

android {
    defaultConfig {
        minSdk = 26
    }
}
