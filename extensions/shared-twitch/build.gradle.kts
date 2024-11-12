extension {
    name = "extensions/shared-twitch.rve"
}

android {
    namespace = "app.revanced.extension"

    buildTypes["release"].isMinifyEnabled = true
}

dependencies {
    compileOnly(libs.okhttp)
}
