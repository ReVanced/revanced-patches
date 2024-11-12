extension {
    name = "extensions/shared-tiktok.rve"
}

android {
    namespace = "app.revanced.extension"

    buildTypes["release"].isMinifyEnabled = true
}

dependencies {
    implementation(project(":extensions:shared"))
    compileOnly(project(":extensions:shared-tiktok:stub"))
}
