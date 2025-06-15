import com.google.protobuf.gradle.id

plugins {
    id("com.google.protobuf") version "0.9.4"
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:spotify:stub"))
    compileOnly(libs.annotation)

    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("com.google.protobuf:protobuf-javalite:3.25.3")
    implementation("io.grpc:grpc-stub:1.56.0")
    implementation("io.grpc:grpc-protobuf-lite:1.56.0")
}

android {
    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.3"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.56.0"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
            task.plugins {
                id("grpc") { }
            }
        }
    }
}
