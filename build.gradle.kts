plugins {
    kotlin("jvm") version "1.6.20"
    java
    `maven-publish`
}

group = "app.revanced"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://maven.pkg.github.com/revanced/revanced-patcher") // note the "r"!
        credentials {
            // DO NOT set these variables in the project's gradle.properties.
            // Instead, you should set them in:
            // Windows: %homepath%\.gradle\gradle.properties
            // Linux: ~/.gradle/gradle.properties
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR") // DO NOT CHANGE!
            password = project.findProperty("gpr.key")  as String? ?: System.getenv("GITHUB_TOKEN") // DO NOT CHANGE!
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))

    implementation("app.revanced:revanced-patcher:1.0.0-dev.9")
}

java {
    withSourcesJar()
    withJavadocJar()
}

val isGitHubCI = System.getenv("GITHUB_ACTOR") != null

publishing {
    repositories {
        if (isGitHubCI) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/revanced/revanced-patches") // note the "s"!
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        } else {
            mavenLocal()
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}
