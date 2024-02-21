rootProject.name = "revanced-patches"

buildCache {
    local {
        isEnabled = "CI" !in System.getenv()
    }
}
