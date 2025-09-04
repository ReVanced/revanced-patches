package app.revanced.patches.instagram.hide.stories
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideStoriesPatch = bytecodePatch(
    name = "Hide Stories",
    description = "Hides Stories from the main page.",
    use = false
) {
    compatibleWith("com.instagram.android")

    execute {
        val addStoryMethod = getOrCreateAvatarViewFingerprint.method
        val addStoryEndIndex = getOrCreateAvatarViewFingerprint.patternMatch!!.endIndex

        // Remove addView of Story, when it's created.
        addStoryMethod.removeInstruction(addStoryEndIndex)
    }
}