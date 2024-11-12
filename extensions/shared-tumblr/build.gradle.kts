extension {
    name = "extensions/shared-tumblr.rve"
}

android {
    namespace = "app.revanced.extension"

    buildTypes["release"].isMinifyEnabled = true
}

dependencies {
    compileOnly(project(":extensions:shared-tumblr:stub"))
}
