extension {
    name = "extensions/shared/tudortmund.rve"
}

android {
    namespace = "app.revanced.extension"

    buildTypes["release"].isMinifyEnabled = true
}

dependencies {
    compileOnly(libs.appcompat)
}
