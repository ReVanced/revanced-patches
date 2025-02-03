//noinspection GradleDependency
android.compileSdk = 33

dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:youtube:stub"))
    compileOnly(libs.annotation)
}
