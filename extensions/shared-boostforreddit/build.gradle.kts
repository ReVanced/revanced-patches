extension {
    name = "extensions/shared/boostforreddit.rve"
}

android {
    namespace = "app.revanced.extension"

    buildTypes["release"].isMinifyEnabled = true
}

dependencies {
    compileOnly(project(":shared"))
    compileOnly(project(":extensions:shared-boostforreddit:stub"))
}
