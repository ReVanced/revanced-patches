package app.revanced.patches.instagram.hide.explore

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val hideExploreFeedPatch = bytecodePatch(
    name = "Hide explore feed",
    description = "Hides posts and reels from the explore/search page.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        exploreResponseJsonParserMethodMatch.method.apply {
            val targetStringIndex = exploreResponseJsonParserMethodMatch[0]
            val targetStringRegister = getInstruction<OneRegisterInstruction>(targetStringIndex).registerA

            replaceInstruction(targetStringIndex, "const-string v$targetStringRegister, \"BOGUS\"")
        }
    }
}
