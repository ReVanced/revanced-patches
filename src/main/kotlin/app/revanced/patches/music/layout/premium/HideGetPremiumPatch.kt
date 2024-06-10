package app.revanced.patches.music.layout.premium

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.music.layout.premium.fingerprints.hideGetPremiumFingerprint
import app.revanced.patches.music.layout.premium.fingerprints.membershipSettingsFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Suppress("unused")
val hideGetPremiumPatch = bytecodePatch(
    name = "Hide 'Get Music Premium' label",
    description = "Hides the \"Get Music Premium\" label from the account menu and settings.",
) {
    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "6.45.54",
            "6.51.53",
            "7.01.53",
            "7.02.52",
            "7.03.52",
        ),
    )

    val hideGetPremiumResult by hideGetPremiumFingerprint
    val membershipSettingsResult by membershipSettingsFingerprint

    execute {
        hideGetPremiumResult.mutableMethod.apply {
            val insertIndex = hideGetPremiumResult.scanResult.patternScanResult!!.endIndex

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
    }

    membershipSettingsResult.mutableMethod.addInstructions(
        0,
        """
            const/4 v0, 0x0
            return-object v0
        """,
    )
}
