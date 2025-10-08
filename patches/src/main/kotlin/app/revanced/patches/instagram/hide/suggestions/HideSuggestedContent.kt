package app.revanced.patches.instagram.hide.suggestions

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val hideSuggestedContent = bytecodePatch(
    name = "Hide suggested content",
    description = "Hides suggested stories, reels, threads and survey from feed (Suggested posts will still be shown).",
    use = false,
) {
    compatibleWith("com.instagram.android")

    execute {
        feedItemParseFromJsonFingerprint.let {

            FEED_ITEM_KEYS.forEach { key ->
                val stringMatchIndex = it.stringMatches?.first { match -> match.string == key }!!.index

                it.method.apply {
                    val stringRegister = getInstruction<OneRegisterInstruction>(stringMatchIndex).registerA

                    // Have a dummy key such that the comparison fails eventually the data is not parsed.
                    addInstruction(
                        stringMatchIndex + 1,
                        "const-string/jumbo v$stringRegister, \"revanced_dummy\"",
                    )
                }
            }
        }
    }
}
