package app.revanced.patches.instagram.hide.highlightsTray

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val hideHighlightsTrayPatch = bytecodePatch(
    name = "Hide highlights tray",
    description = "Hides the highlights tray in profile section.",
    use = false,
) {
    compatibleWith("com.instagram.android")

    apply {
        highlightsUrlBuilderMethodMatch.method.apply {
            val targetStringIndex = highlightsUrlBuilderMethodMatch[0]
            val targetStringRegister = getInstruction<OneRegisterInstruction>(targetStringIndex).registerA

            replaceInstruction(targetStringIndex, "const-string v$targetStringRegister, \"BOGUS\"")
        }
    }
}
