extension {
    name = "extensions/shared/syncforreddit.rve"
}

android {
    namespace = "app.revanced.extension"

    buildTypes["release"].isMinifyEnabled = true
}

dependencies {
    compileOnly(project(":shared"))
    compileOnly(project(":extensions:shared-syncforreddit:stub"))
    compileOnly(libs.annotation)
}
