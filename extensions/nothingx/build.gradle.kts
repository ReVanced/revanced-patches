dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:nothingx:stub"))
}

android {
    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}