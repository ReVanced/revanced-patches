plugins {
    alias(libs.plugins.protobuf)
}

dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:spotify:stub"))
    compileOnly(libs.annotation)

    implementation(libs.nanohttpd)
    implementation(libs.protobuf.javalite)
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
        artifact = libs.protobuf.protoc.get().toString()
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}
