package app.revanced.patches.googlenews.customtabs

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val enableCustomTabsPatch = bytecodePatch(
    name = "Enable CustomTabs",
    description = "Enables CustomTabs to open articles in your default browser.",
) {
    compatibleWith("com.google.android.apps.magazines")

    execute {
        launchCustomTabFingerprint.method.apply {
            val checkIndex = launchCustomTabFingerprint.patternMatch!!.endIndex + 1
            val register = getInstruction<OneRegisterInstruction>(checkIndex).registerA

            replaceInstruction(checkIndex, "const/4 v$register, 0x1")
        }
    }
}
