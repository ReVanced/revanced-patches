package app.revanced.patches.instagram.hide.suggestions

import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val hideSuggestedContentPatch = bytecodePatch(
    name = "Hide suggested content",
    description = "Hides suggested stories, reels, threads and survey from feed (Suggested posts will still be shown).",
    use = false,
) {
    compatibleWith("com.instagram.android"("421.0.0.51.66"))

    apply {
        feedItemParseFromJsonMethodMatch.method.apply {
            val instrs = implementation?.instructions?.toList() ?: return@apply
            instrs.forEachIndexed { index, instruction ->
                if (instruction is ReferenceInstruction) {
                    val reference = instruction.reference
                    if (reference is StringReference) {
                        if (reference.string in FEED_ITEM_KEYS_TO_BE_HIDDEN) {
                            if (instruction is OneRegisterInstruction) {
                                val targetStringRegister = instruction.registerA
                                replaceInstruction(index, "const-string v$targetStringRegister, \"BOGUS\"")
                            }
                        }
                    }
                }
            }
        }
    }
}
