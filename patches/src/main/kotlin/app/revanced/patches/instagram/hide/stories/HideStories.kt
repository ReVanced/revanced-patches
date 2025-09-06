package app.revanced.patches.instagram.hide.stories
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideStoriesPatch = bytecodePatch(
    name = "Hide Stories from Home",
    description = "Hides Stories from the main page, by removing the buttons.",
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        val addStoryMethod = getOrCreateAvatarViewFingerprint.method // Creates Story
        val addStoryEndIndex = getOrCreateAvatarViewFingerprint.patternMatch!!.endIndex

        // Remove addView of Story.
        addStoryMethod.removeInstruction(addStoryEndIndex)
    }
}