package app.revanced.patches.youtube.layout.spoofappversion

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.ArrayResource
import app.revanced.patches.shared.settings.preference.impl.ListPreference
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.spoofappversion.fingerprints.SpoofAppVersionFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.strings.StringsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Spoof app version",
    description = "Tricks YouTube into thinking you are running an older version of the app. " +
            "This can be used to restore old UI elements and features.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.32.39",
                "18.37.36",
                "18.38.44",
                "18.43.45",
                "18.44.41",
                "18.45.41",
                "18.45.43"
            ]
        )
    ]
)
@Suppress("unused")
object SpoofAppVersionPatch : BytecodePatch(
    setOf(SpoofAppVersionFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/spoof/SpoofAppVersionPatch;"

    override fun execute(context: BytecodeContext) {
        StringsPatch.includePatchStrings("SpoofAppVersion")
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_spoof_app_version",
                "revanced_spoof_app_version_title",
                "revanced_spoof_app_version_summary_on",
                "revanced_spoof_app_version_summary_off"
            ),
            ListPreference(
                "revanced_spoof_app_version_target",
                "revanced_spoof_app_version_target_title",
                ArrayResource(
                    "revanced_spoof_app_version_target_entries",
                    listOf(
                        "revanced_spoof_app_version_target_entry_1",
                        "revanced_spoof_app_version_target_entry_2",
                        "revanced_spoof_app_version_target_entry_3",
                        "revanced_spoof_app_version_target_entry_4",
                        "revanced_spoof_app_version_target_entry_5",
                    )
                ),
                ArrayResource(
                    "revanced_spoof_app_version_target_entry_values",
                    listOf(
                        "18.33.40",
                        "18.20.39",
                        "17.08.35",
                        "16.08.35",
                        "16.01.35",
                    ),
                    literalValues = true
                )
            )
        )

        SpoofAppVersionFingerprint.result?.apply {
            val insertIndex = scanResult.patternScanResult!!.startIndex + 1
            val buildOverrideNameRegister =
                (mutableMethod.implementation!!.instructions[insertIndex - 1] as OneRegisterInstruction).registerA

            mutableMethod.addInstructions(
                insertIndex,
                """
                    invoke-static {v$buildOverrideNameRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->getYouTubeVersionOverride(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$buildOverrideNameRegister
                """
            )
        } ?: throw SpoofAppVersionFingerprint.exception
    }
}
