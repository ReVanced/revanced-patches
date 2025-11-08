package app.revanced.patches.instagram.ghost.story

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val anonymousStoryWatchingPatch = bytecodePatch(
    name = "Anonymous story watching",
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        // Prevent the hashmap of the seen media to be filled
        setMediaSeenHashmapFingerprint.method.returnEarly()
    }
}
