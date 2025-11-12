package app.revanced.patches.instagram.story.flipping

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableStoryAutoFlippingPatch = bytecodePatch(
    name = "Disable story auto flipping",
    description = "Disable stories automatically flipping/skipping after some seconds.",
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        onStoryTimeoutActionFingerprint.method.returnEarly()
    }
}
