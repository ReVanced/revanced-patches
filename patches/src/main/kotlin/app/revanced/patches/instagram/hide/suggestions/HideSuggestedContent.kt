package app.revanced.patches.instagram.hide.suggestions

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val hideSuggestedContent = bytecodePatch(
    name = "Hide suggested content",
    description = "Hides suggested stories, reels, threads and survey from feed (Suggested posts will still be shown).",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        feedItemParseFromJsonMethodMatch.method.apply {
            feedItemParseFromJsonMethodMatch.indices[0].forEach { targetStringIndex ->
                val targetStringRegister = getInstruction<OneRegisterInstruction>(targetStringIndex).registerA

                replaceInstruction(targetStringIndex, "const-string v$targetStringRegister, \"BOGUS\"")
            }
        }
    }
}
