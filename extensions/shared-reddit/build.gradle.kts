extension {
    name = "extensions/shared/reddit.rve"
}

android {
    namespace = "app.revanced.extension"

    buildTypes["release"].isMinifyEnabled = true
}

dependencies {
    compileOnly(project(":extensions:shared-reddit:stub"))
}
