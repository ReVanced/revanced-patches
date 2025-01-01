group = "app.revanced"

patches {
    about {
        name = "ReVanced Patches"
        description = "Patches for ReVanced"
        source = "git@github.com:revanced/revanced-patches.git"
        author = "ReVanced"
        contact = "contact@revanced.app"
        website = "https://revanced.app"
        license = "GNU General Public License v3.0"
    }
}

repositories {
    mavenLocal()
    gradlePluginPortal()
    google()
}

dependencies {
    // Used by JsonGenerator.
    implementation(libs.gson)
    // Required due to smali, or build fails. Can be removed once smali is bumped.
    implementation(libs.guava)
    // Android API stubs defined here.
    compileOnly(project(":patches:stub"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/revanced/revanced-patches")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
