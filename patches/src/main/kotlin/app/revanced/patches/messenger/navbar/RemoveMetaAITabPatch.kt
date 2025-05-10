package app.revanced.patches.messenger.navbar

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val removeMetaAITabPatch = bytecodePatch(
    name = "Remove Meta AI tab",
    description = "Removes the 'Meta AI' tab from the navbar.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        createTabConfigurationFingerprint.let {
            val moveResultIndex = it.instructionMatches.first().index + 1
            val enabledRegister = it.method.getInstruction<OneRegisterInstruction>(moveResultIndex).registerA
            it.method.replaceInstruction(
                moveResultIndex,
                "const/4 v$enabledRegister, 0x0"
            )
        }
    }
}
