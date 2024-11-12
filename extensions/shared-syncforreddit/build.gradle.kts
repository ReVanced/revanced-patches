extension {
    name = "extensions/shared-syncforreddit.rve"
}

android {
    namespace = "app.revanced.extension"

    buildTypes["release"].isMinifyEnabled = true
}

dependencies {
    implementation(project(":extensions:shared"))
    compileOnly(project(":extensions:shared-syncforreddit:stub"))
    compileOnly(libs.annotation)
}
