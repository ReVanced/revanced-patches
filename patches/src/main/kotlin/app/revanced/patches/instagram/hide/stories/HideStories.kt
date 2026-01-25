package app.revanced.patches.instagram.hide.stories

import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Hide Stories from Home` by creatingBytecodePatch(
    description = "Hides Stories from the main page, by removing the buttons.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        getOrCreateAvatarViewMethodMatch.let {
            val addStoryEndIndex = it.indices.last()

            // Remove addView of Story.
            it.method.removeInstruction(addStoryEndIndex)
        }
    }
}
