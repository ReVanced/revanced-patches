package app.revanced.patches.youtube.misc.privacy

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.privacy.fingerprints.CopyTextFingerprint
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(
    name = "Remove tracking query parameter",
    description = "Remove the tracking query parameter from links.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.43.45",
                "18.44.41",
                "18.45.41"
            ]
        )
    ]
)
@Suppress("unused")
object RemoveTrackingQueryParameterPatch : BytecodePatch(
    setOf(CopyTextFingerprint)
) {
    const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/RemoveTrackingQueryParameterPatch;"

    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_remove_tracking_query_parameter",
                StringResource(
                    "revanced_remove_tracking_query_parameter_title",
                    "Remove tracking query parameter"
                ),
                StringResource(
                    "revanced_remove_tracking_query_parameter_summary_on",
                    "Tracking query parameter is removed from links"
                ),
                StringResource(
                    "revanced_remove_tracking_query_parameter_summary_off",
                    "Tracking query parameter is not removed from links"
                ),
            )
        )

        CopyTextFingerprint.result?.let {
            val insertIndex = it.scanResult.patternScanResult!!.startIndex + 2

            it.mutableMethod.apply {
                val urlRegister = getInstruction<TwoRegisterInstruction>(insertIndex - 2).registerA

                addInstructions(
                    insertIndex,
                    """
                        invoke-static {v$urlRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->sanitize(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$urlRegister
                    """
                )
            }

        }
    }
}