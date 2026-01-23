package app.revanced.patches.instagram.hide.suggestions

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.creatingBytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused", "ObjectPropertyName")
val `Hide suggested content` by creatingBytecodePatch(
    description = "Hides suggested stories, reels, threads and survey from feed (Suggested posts will still be shown).",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        feedItemParseFromJsonMethodMatch.method.apply {
            feedItemParseFromJsonMethodMatch.indices.forEach { targetStringIndex ->
                val targetStringRegister = getInstruction<OneRegisterInstruction>(targetStringIndex).registerA

                replaceInstruction(targetStringIndex, "const-string v$targetStringRegister, \"BOGUS\"")
            }
        }
    }
}
