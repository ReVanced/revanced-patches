import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val internalize by configurations.creating

dependencies {
    internalize("xyz.gianlu.librespot:librespot-player:1.6.5:thin") {
        exclude(group = "xyz.gianlu.librespot", module = "librespot-sink")
        exclude(group = "com.lmax", module = "disruptor")
        exclude(group = "org.apache.logging.log4j")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "org.jetbrains", module = "annotations")
    }

    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:spotify:stub"))
    compileOnly(libs.annotation)

    implementation(project(":extensions:spotify:utils"))
    implementation(libs.nanohttpd)
}

val relocateTask by tasks.register<ShadowJar>("relocateTask") {
    configurations = listOf(internalize)

    relocate("okhttp3", "app.revanced.extensions.spotify.okhttp3")
}

tasks.build.dependsOn(relocateTask)

dependencies {
    implementation(relocateTask.outputs.files)
}

android {
    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packaging {
        resources {
            excludes += "log4j2.xml"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}
