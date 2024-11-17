extension {
    name = "extensions/shared/youtube.rve"
}

android {
    namespace = "app.revanced.extension"

    buildTypes["release"].isMinifyEnabled = true
}

dependencies {
    compileOnly(project(":shared"))
    compileOnly(project(":extensions:shared-youtube:stub"))
    compileOnly(libs.annotation)
}