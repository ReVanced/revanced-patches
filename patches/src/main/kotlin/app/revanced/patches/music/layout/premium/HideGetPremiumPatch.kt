package app.revanced.patches.music.layout.premium

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

val hideGetPremiumPatch = bytecodePatch(
    name = "Hide 'Get Music Premium' label",
    description = "Hides the \"Get Music Premium\" label from the account menu and settings.",
) {
    compatibleWith("com.google.android.apps.youtube.music")

    execute {
        hideGetPremiumFingerprint.method.apply {
            val insertIndex = hideGetPremiumFingerprint.filterMatches.last().index

            val setVisibilityInstruction = getInstruction<FiveRegisterInstruction>(insertIndex)
            val getPremiumViewRegister = setVisibilityInstruction.registerC
            val visibilityRegister = setVisibilityInstruction.registerD

            replaceInstruction(
                insertIndex,
                "const/16 v$visibilityRegister, 0x8",
            )

            addInstruction(
                insertIndex + 1,
                "invoke-virtual {v$getPremiumViewRegister, v$visibilityRegister}, " +
                    "Landroid/view/View;->setVisibility(I)V",
            )
        }

        membershipSettingsFingerprint.method.addInstructions(
            0,
            """
            const/4 v0, 0x0
            return-object v0
        """,
        )
    }
}
