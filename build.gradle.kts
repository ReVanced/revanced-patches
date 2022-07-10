plugins {
    kotlin("jvm") version "1.7.0"
}

group = "app.revanced"

val githubUsername: String = project.findProperty("gpr.user") as? String ?: System.getenv("GITHUB_ACTOR")
val githubPassword: String = project.findProperty("gpr.key") as? String ?: System.getenv("GITHUB_TOKEN")

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://maven.pkg.github.com/revanced/revanced-patcher")
        credentials {
            username = githubUsername
            password = githubPassword
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("app.revanced:revanced-patcher:2.4.0")
    implementation("app.revanced:multidexlib2:2.5.2.r2")
}

tasks {
    register<DefaultTask>("generateDex") {
        description = "Generate dex files from build"
        dependsOn(build)

        doLast {
            val androidHome = System.getenv("ANDROID_HOME") ?: throw GradleException("ANDROID_HOME not found")
            val d8 = "${androidHome}/build-tools/32.0.0/d8"
            val input = configurations.archives.get().allArtifacts.files.files.first().absolutePath
            val output = input.replace(".jar", ".dex")
            val work = File("${buildDir}/libs")

            exec {
                workingDir = work
                commandLine = listOf(d8, input)
            }

            exec {
                workingDir = work
                commandLine = listOf("mv", "classes.dex", output)
            }
        }
    }
    register<JavaExec>("generateReadme") {
        description = "Generate README.md"
        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("app.revanced.meta.readme.Generator")
    }
    // Dummy task to fix the Gradle semantic-release plugin.
    // Remove this if you forked it to support building only.
    // Tracking issue: https://github.com/KengoTODA/gradle-semantic-release-plugin/issues/435
    register<DefaultTask>("publish") {
        group = "publish"
        description = "Dummy task"
        dependsOn(named("generateDex"), named("generateReadme"))
    }
}
