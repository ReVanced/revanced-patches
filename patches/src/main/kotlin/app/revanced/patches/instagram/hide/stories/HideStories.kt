package app.revanced.patches.instagram.hide.stories

import app.revanced.patcher.extensions.removeInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Hide Stories from Home` by creatingBytecodePatch(
    description = "Hides Stories from the main page, by removing the buttons.",
    use = false
) {
    compatibleWith("com.instagram.android")

    apply {
        val addStoryMethod = getOrCreateAvatarViewFingerprint.method // Creates Story
        val addStoryEndIndex = getOrCreateAvatarViewFingerprint.patternMatch.endIndex

        // Remove addView of Story.
        addStoryMethod.removeInstruction(addStoryEndIndex)
    }
}