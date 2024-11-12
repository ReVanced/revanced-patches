extension {
    name = "extensions/shared-boostforreddit.rve"
}

android {
    namespace = "app.revanced.extension"

    buildTypes["release"].isMinifyEnabled = true
}

dependencies {
    implementation(project(":extensions:shared"))
    compileOnly(project(":extensions:shared-boostforreddit:stub"))
}
