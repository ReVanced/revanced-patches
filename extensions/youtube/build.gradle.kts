dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:youtube:stub"))
    compileOnly(libs.annotation)
}

android {
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }
}
