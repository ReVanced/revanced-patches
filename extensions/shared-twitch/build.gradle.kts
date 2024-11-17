extension {
    name = "extensions/shared/twitch.rve"
}

android {
    namespace = "app.revanced.extension"

    buildTypes["release"].isMinifyEnabled = true
}

dependencies {
    compileOnly(project(":shared"))
    compileOnly(project(":extensions:shared-twitch:stub"))
    compileOnly(libs.okhttp)
    compileOnly(libs.retrofit)
    compileOnly(libs.annotation)
    compileOnly(libs.appcompat)
}
