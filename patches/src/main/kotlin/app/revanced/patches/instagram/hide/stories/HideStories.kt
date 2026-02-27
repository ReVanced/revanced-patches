package app.revanced.patches.instagram.hide.stories

import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideStoriesFromHomePatch = bytecodePatch(
    name = "Hide Stories from Home",
    description = "Hides Stories from the main page, by removing the buttons.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        getOrCreateAvatarViewMethodMatch.let {
            val addStoryEndIndex = it[-1]

            // Remove addView of Story.
            it.method.removeInstruction(addStoryEndIndex)
        }
    }
}
