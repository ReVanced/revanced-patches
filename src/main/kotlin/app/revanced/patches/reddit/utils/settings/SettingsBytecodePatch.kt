package app.revanced.patches.reddit.utils.settings

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.reddit.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.reddit.utils.resourceid.SharedResourceIdPatch.LabelAcknowledgements
import app.revanced.patches.reddit.utils.settings.fingerprints.AcknowledgementsLabelBuilderFingerprint
import app.revanced.patches.reddit.utils.settings.fingerprints.OssLicensesMenuActivityOnCreateFingerprint
import app.revanced.patches.reddit.utils.settings.fingerprints.SettingsStatusLoadFingerprint
import app.revanced.util.exception
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(dependencies = [SharedResourceIdPatch::class])
object SettingsBytecodePatch : BytecodePatch(
    setOf(
        AcknowledgementsLabelBuilderFingerprint,
        OssLicensesMenuActivityOnCreateFingerprint,
        SettingsStatusLoadFingerprint
    )
) {
    private const val INTEGRATIONS_METHOD_DESCRIPTOR =
        "Lapp/revanced/integrations/reddit/settingsmenu/ReVancedSettingActivity;->initializeSettings(Landroid/app/Activity;)V"

    private lateinit var settingsMethod: MutableMethod

    internal fun updateSettingsStatus(description: String) {
        settingsMethod.apply {
            addInstruction(
                0,
                "invoke-static {}, Lapp/revanced/integrations/reddit/settingsmenu/SettingsStatus;->$description()V"
            )
        }
    }

    override fun execute(context: BytecodeContext) {

        /**
         * Replace settings label
         */
        AcknowledgementsLabelBuilderFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex =
                    getWideLiteralInstructionIndex(LabelAcknowledgements) + 3
                val insertRegister =
                    getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA

                addInstruction(
                    insertIndex,
                    "const-string v$insertRegister, \"ReVanced Extended\""
                )
            }
        } ?: throw AcknowledgementsLabelBuilderFingerprint.exception

        /**
         * Initialize settings activity
         */
        OssLicensesMenuActivityOnCreateFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = it.scanResult.patternScanResult!!.startIndex + 1

                addInstructions(
                    insertIndex, """
                        invoke-static {p0}, $INTEGRATIONS_METHOD_DESCRIPTOR
                        return-void
                        """
                )
            }
        } ?: throw OssLicensesMenuActivityOnCreateFingerprint.exception

        settingsMethod = SettingsStatusLoadFingerprint.result?.mutableMethod
            ?: throw SettingsStatusLoadFingerprint.exception

    }
}