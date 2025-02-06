dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:youtube:stub"))
    compileOnly(libs.annotation)
}

android {
    compileSdk = 33 // TODO: Update Swipe controls code to allow updating this to the latest sdk.

    defaultConfig {
        minSdk = 26
    }
}
