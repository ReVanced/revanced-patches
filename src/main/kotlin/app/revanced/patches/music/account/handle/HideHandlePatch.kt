package app.revanced.patches.music.account.handle

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.account.handle.fingerprints.AccountSwitcherAccessibilityLabelFingerprint
import app.revanced.patches.music.account.handle.fingerprints.NamesInactiveAccountThumbnailSizeFingerprint
import app.revanced.patches.music.utils.integrations.Constants.ACCOUNT
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Hide handle",
    description = "Adds an option to hide the handle in the account menu.",
    dependencies = [
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object HideHandlePatch : BytecodePatch(
    setOf(
        AccountSwitcherAccessibilityLabelFingerprint,
        NamesInactiveAccountThumbnailSizeFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        /**
         * Hide handle in account menu
         */
        AccountSwitcherAccessibilityLabelFingerprint.result?.let { result ->
            result.mutableMethod.apply {

                val textColorIndex = indexOfFirstInstruction {
                    getReference<MethodReference>()?.name == "setTextColor"
                }
                val setTextIndex = implementation!!.instructions.let {
                    textColorIndex + it.subList(textColorIndex, textColorIndex + 10).indexOfFirst { instruction ->
                        instruction.opcode == Opcode.INVOKE_VIRTUAL
                                && instruction.getReference<MethodReference>()?.name == "setVisibility"
                    }
                }
                val textViewInstruction = getInstruction<Instruction35c>(setTextIndex)

                replaceInstruction(
                    setTextIndex,
                    "invoke-static {v${textViewInstruction.registerC}, v${textViewInstruction.registerD}}, $ACCOUNT->hideHandle(Landroid/widget/TextView;I)V"
                )
            }
        } ?: throw AccountSwitcherAccessibilityLabelFingerprint.exception

        /**
         * Hide handle in account switcher
         */
        NamesInactiveAccountThumbnailSizeFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = it.scanResult.patternScanResult!!.startIndex
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstructions(
                    targetIndex, """
                        invoke-static {v$targetRegister}, $ACCOUNT->hideHandle(Z)Z
                        move-result v$targetRegister
                        """
                )
            }
        } ?: throw NamesInactiveAccountThumbnailSizeFingerprint.exception

        SettingsPatch.addMusicPreference(
            CategoryType.ACCOUNT,
            "revanced_hide_handle",
            "true"
        )

    }
}
