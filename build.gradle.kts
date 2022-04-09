plugins {
    kotlin("jvm") version "1.6.10"
    java
    `maven-publish`
}

group = "app.revanced"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/ReVancedTeam/revanced-patcher") // note the "r"!
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")

    implementation(files("P:\\Andere Dateien\\STUFF\\Coding\\Java\\revanced\\revanced-patcher\\build\\libs\\revanced-patcher-1.0.0-dev.8.jar")) // use latest version.
    implementation("org.smali:dexlib2:2.5.2")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ReVancedTeam/revanced-patches") // note the "s"!
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}