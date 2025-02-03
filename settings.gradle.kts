rootProject.name = "revanced-patches"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/revanced/registry")
            credentials {
                username = providers.gradleProperty("gpr.user").getOrElse(System.getenv("GITHUB_ACTOR"))
                password = providers.gradleProperty("gpr.key").getOrElse(System.getenv("GITHUB_TOKEN"))
            }
        }
    }
}

plugins {
    id("app.revanced.patches") version "1.0.0-dev.7"
}

settings {
    extensions {
        defaultNamespace = "app.revanced.extension"

        // Must use a file to convert relative path to absolute path,
        // otherwise the extensions in subfolders fail to find the proguard config.
        proguardFiles(file("extensions/proguard-rules.pro").toString())
    }
}

include(":patches:stub")
