package app.revanced.patches.music.utils.settings

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.utils.integrations.Constants.INTEGRATIONS_PATH
import app.revanced.patches.music.utils.integrations.IntegrationsPatch
import app.revanced.patches.music.utils.mainactivity.MainActivityResolvePatch
import app.revanced.patches.music.utils.mainactivity.MainActivityResolvePatch.injectInit
import app.revanced.patches.music.utils.settings.fingerprints.PreferenceFingerprint
import app.revanced.patches.music.utils.settings.fingerprints.SettingsHeadersFragmentFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    dependencies = [
        IntegrationsPatch::class,
        MainActivityResolvePatch::class
    ],
    requiresIntegrations = true
)
object SettingsBytecodePatch : BytecodePatch(
    setOf(
        PreferenceFingerprint,
        SettingsHeadersFragmentFingerprint
    )
) {
    private const val INTEGRATIONS_ACTIVITY_CLASS_DESCRIPTOR =
        "$INTEGRATIONS_PATH/settingsmenu/ReVancedSettingActivity;"
    private const val INTEGRATIONS_FRAGMENT_CLASS_DESCRIPTOR =
        "$INTEGRATIONS_PATH/settingsmenu/ReVancedSettingsFragment;"

    override fun execute(context: BytecodeContext) {

        /**
         * Inject settings Activity.
         */
        SettingsHeadersFragmentFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = it.scanResult.patternScanResult!!.endIndex
                val targetRegister = getInstruction<OneRegisterInstruction>(targetIndex).registerA

                addInstruction(
                    targetIndex + 1,
                    "invoke-static {v$targetRegister}, $INTEGRATIONS_ACTIVITY_CLASS_DESCRIPTOR->setActivity(Ljava/lang/Object;)V"
                )
            }
        } ?: throw SettingsHeadersFragmentFingerprint.exception

        /**
         * Values are loaded when preferences change.
         */
        PreferenceFingerprint.result?.let {
            it.mutableMethod.apply {
                val targetIndex = it.scanResult.patternScanResult!!.endIndex
                val keyRegister = getInstruction<FiveRegisterInstruction>(targetIndex).registerD
                val valueRegister = getInstruction<FiveRegisterInstruction>(targetIndex).registerE

                addInstruction(
                    targetIndex,
                    "invoke-static {v$keyRegister, v$valueRegister}, $INTEGRATIONS_FRAGMENT_CLASS_DESCRIPTOR->onPreferenceChanged(Ljava/lang/String;Z)V"
                )
            }
        } ?: throw PreferenceFingerprint.exception

        injectInit("InitializationPatch", "setDeviceInformation")
        injectInit("InitializationPatch", "initializeReVancedSettings")

    }
}
