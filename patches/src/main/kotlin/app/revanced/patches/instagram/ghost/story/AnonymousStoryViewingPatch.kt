package app.revanced.patches.instagram.ghost.story

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.Utils.trimIndentMultiline
import app.revanced.util.returnEarly

@Suppress("unused")
val anonymousStoryViewingPatch = bytecodePatch(
    name = "Anonymous story viewing",
    description = """
        View stories without sending any information to the server. 
        Your view will not appear in the story viewers list. 
        Note: Since no data is sent, a story you have already viewed may appear as new on another device.
    """.trimIndentMultiline(),
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        // Prevent the hashmap of the seen media to be filled
        setMediaSeenHashmapFingerprint.method.returnEarly()
    }
}
