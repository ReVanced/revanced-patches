package app.revanced.patches.tiktok.misc.settings

import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.tiktok.misc.extension.sharedExtensionPatch
import app.revanced.util.findFreeRegister
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/tiktok/settings/AdPersonalizationActivityHook;"

val settingsPatch = bytecodePatch(
    name = "Settings",
    description = "Adds ReVanced settings to TikTok.",
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith(
        "com.ss.android.ugc.trill"("39.8.1"),
        "com.zhiliaoapp.musically"("39.8.1"),
    )

    execute {
        val adPersonalizationActivityClassName =
            "Lcom/bytedance/ies/ugc/aweme/commercialize/compliance/personalization/AdPersonalizationActivity;"
        val initializeSettingsMethodDescriptor =
            "$EXTENSION_CLASS_DESCRIPTOR->initialize($adPersonalizationActivityClassName)Z"

        // Patch AdPersonalizationActivity so that when the extension calls startActivity for it,
        // the settings menu can be initialized.
        adPersonalizationActivityOnCreateFingerprint.method.apply {
            val invokeSuperIndex = indexOfFirstInstruction(Opcode.INVOKE_SUPER)
            val instructionAfterInvokeSuper = getInstruction(invokeSuperIndex + 1)

            val thisRegister = getInstruction<FiveRegisterInstruction>(invokeSuperIndex).registerC
            val freeRegister = findFreeRegister(invokeSuperIndex)

            addInstructionsWithLabels(
                invokeSuperIndex + 1,
                """
                    invoke-static { v$thisRegister }, $initializeSettingsMethodDescriptor
                    move-result v$freeRegister
                    if-eqz v$freeRegister, :skip_opening_revanced_settings
                    return-void
                """,
                ExternalLabel("skip_opening_revanced_settings", instructionAfterInvokeSuper),
            )
        }
    }
}
