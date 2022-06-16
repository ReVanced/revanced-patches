plugins {
    kotlin("jvm") version "1.7.0"
}

group = "app.revanced"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/revanced/revanced-patcher")
        credentials {
            username = project.findProperty("gpr.user") as? String ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key")  as? String ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("app.revanced:revanced-patcher:1.1.0")
}

tasks {
    // Dummy task to fix the Gradle semantic-release plugin.
    // Remove this if you forked it to support building only.
    // Tracking issue: https://github.com/KengoTODA/gradle-semantic-release-plugin/issues/435
    register<DefaultTask>("publish") {
        group = "publish"
        description = "Dummy task"
    }
}