extension {
    name = "extensions/shared/tiktok.rve"
}

android {
    namespace = "app.revanced.extension"

    buildTypes["release"].isMinifyEnabled = true
}

dependencies {
    compileOnly(project(":shared"))
    compileOnly(project(":extensions:shared-tiktok:stub"))
    compileOnly(libs.annotation)
}
