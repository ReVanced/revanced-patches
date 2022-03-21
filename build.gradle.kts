plugins {
    kotlin("jvm") version "1.6.10"
    `maven-publish`
}

group = "net.revanced"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/ReVancedTeam/revanced-patcher")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("REVANCED_PATCHER_GITHUB_PACKAGE_TOKEN")
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")

    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-util:9.2")
    implementation("org.ow2.asm:asm-tree:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")

    implementation("net.revanced:revanced-patcher:1.0.0-dev.7")
}