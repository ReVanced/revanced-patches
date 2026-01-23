package app.revanced.patches.instagram.hide.explore

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.creatingBytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused", "ObjectPropertyName")
val `Hide explore feed` by creatingBytecodePatch(
    description = "Hides posts and reels from the explore/search page.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        exploreResponseJsonParserMethodMatch.method.apply {
            val targetStringIndex = exploreResponseJsonParserMethodMatch.indices.first()
            val targetStringRegister = getInstruction<OneRegisterInstruction>(targetStringIndex).registerA

            replaceInstruction(targetStringIndex, "const-string v$targetStringRegister, \"BOGUS\"")
        }
    }
}
