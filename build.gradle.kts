plugins {
    kotlin("jvm") version "1.8.20"
    alias(libs.plugins.ksp)
}

group = "app.revanced"

repositories {
    mavenCentral()
    mavenLocal()
    google()
    maven {
        url = uri("https://maven.pkg.github.com/revanced/revanced-patcher")
        credentials {
            username = project.findProperty("gpr.user") as? String ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as? String ?: System.getenv("GITHUB_TOKEN")
        }
    }
    // Required for FlexVer-Java
    maven {
        url = uri("https://repo.sleeping.town")
        content {
            includeGroup("com.unascribed")
        }
    }
}

dependencies {
    implementation(libs.revanced.patcher)
    implementation(libs.smali)
    implementation(libs.revanced.patch.annotation.processor)
    // TODO: Required because build fails without it. Find a way to remove this dependency.
    implementation(libs.guava)
    // Used in JsonGenerator.
    implementation(libs.gson)
    // A dependency to the Android library unfortunately fails the build, which is why this is required.
    compileOnly(project("dummy"))

    ksp(libs.revanced.patch.annotation.processor)
}

kotlin {
    jvmToolchain(11)
}

tasks {
    register<DefaultTask>("generateBundle") {
        description = "Generate dex files from build and bundle them in the jar file"
        dependsOn(build)

        doLast {
            val androidHome = System.getenv("ANDROID_HOME") ?: throw GradleException("ANDROID_HOME not found")
            val d8 = "${androidHome}/build-tools/33.0.1/d8"
            val input = configurations.archives.get().allArtifacts.files.files.first().absolutePath
            val work = layout.buildDirectory.dir("libs").get().asFile

            exec {
                workingDir = work
                commandLine = listOf(d8, input)
            }

            exec {
                workingDir = work
                commandLine = listOf("zip", "-u", input, "classes.dex")
            }
        }
    }

    register<JavaExec>("generateMeta") {
        description = "Generate metadata for this bundle"
        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("app.revanced.meta.PatchesFileGenerator")
    }

    // Dummy task to fix the Gradle semantic-release plugin.
    // Remove this if you forked it to support building only.
    // Tracking issue: https://github.com/KengoTODA/gradle-semantic-release-plugin/issues/435
    register<DefaultTask>("publish") {
        group = "publish"
        description = "Dummy task"
        dependsOn(named("generateBundle"), named("generateMeta"))
    }
}
