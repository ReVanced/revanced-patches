rootProject.name = "revanced-patches"

buildCache {
    local {
        isEnabled = !System.getenv().containsKey("CI")
    }
}
