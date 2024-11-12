extension {
    name = "extensions/shared.rve"
}

android {
    namespace = "app.revanced.extension"

    buildTypes["release"].isMinifyEnabled = true
}

dependencies {
    compileOnly(libs.annotation)
}
