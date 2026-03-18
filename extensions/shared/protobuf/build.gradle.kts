import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    alias(libs.plugins.protobuf)
    alias(libs.plugins.shadow)
}

val shade: Configuration by configurations.creating {
    configurations.getByName("compileClasspath").extendsFrom(this)
    configurations.getByName("runtimeClasspath").extendsFrom(this)
}

dependencies {
    compileOnly(libs.annotation)
    compileOnly(libs.okhttp)
    shade(libs.protobuf.javalite)
}

sourceSets {
    // Make sure generated proto sources are compiled and end up in the shaded jar
    main {
        java.srcDir("$buildDir/generated/source/proto/main/java")
    }
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                named("java") {
                    option("lite")
                }
            }
        }
    }
}

val shadowJar = tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(shade)
    relocate("com.google.protobuf", "app.revanced.com.google.protobuf")
}

configurations.named("runtimeElements") {
    isCanBeConsumed = true
    isCanBeResolved = false

    outgoing.artifacts.clear()
    outgoing.artifact(shadowJar)
}!!.let { artifacts { add(it.name, shadowJar) } }

