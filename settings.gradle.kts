rootProject.name = "revanced-patches"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        maven {
            name = "githubPackages"
            url = uri("https://maven.pkg.github.com/revanced/revanced-patches-gradle-plugin")
            credentials(PasswordCredentials::class)
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
    }
}

plugins {
    id("app.revanced.patches") version "1.0.0-dev.10"
}

settings {
    extensions {
        defaultNamespace = "app.revanced.extension"

        // Must resolve to an absolute path (not relative),
        // otherwise the extensions in subfolders will fail to find the proguard config.
        proguardFiles(rootProject.projectDir.resolve("extensions/proguard-rules.pro").toString())
    }
}

include(":patches:stub")
