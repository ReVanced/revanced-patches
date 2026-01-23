package app.revanced.patches.instagram.story.flipping

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val `Disable story auto flipping` by creatingBytecodePatch(
    description = "Disable stories automatically flipping/skipping after some seconds.",
    use = false
) {
    compatibleWith("com.instagram.android")

    apply {
        onStoryTimeoutActionMethod.returnEarly()
    }
}
