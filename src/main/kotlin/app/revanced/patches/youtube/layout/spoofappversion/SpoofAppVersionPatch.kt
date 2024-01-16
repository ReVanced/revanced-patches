package app.revanced.patches.youtube.layout.spoofappversion

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.ArrayResource
import app.revanced.patches.shared.settings.preference.impl.ListPreference
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.spoofappversion.fingerprints.SpoofAppVersionFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Spoof app version",
    description = "Adds an option to trick YouTube into thinking you are running an older version of the app. " +
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
                "18.45.43",
                "18.48.39",
                "18.49.37",
                "19.01.34"
            ]
        )
    ]
)
@Suppress("unused")
object SpoofAppVersionPatch : BytecodePatch(
    setOf(SpoofAppVersionFingerprint)
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/youtube/patches/spoof/SpoofAppVersionPatch;"

    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.LAYOUT.addPreferences(
            SwitchPreference(
                "revanced_spoof_app_version",
                StringResource("revanced_spoof_app_version_title", "Spoof app version"),
                StringResource("revanced_spoof_app_version_summary_on", "Version spoofed"),
                StringResource("revanced_spoof_app_version_summary_off", "Version not spoofed"),
                StringResource("revanced_spoof_app_version_user_dialog_message",
                "App version will be spoofed to an older version of YouTube."
                        + "\\n\\nThis will change the appearance and features of the app, but unknown side effects may occur."
                        + "\\n\\nIf later turned off, it is recommended to clear the app data to prevent UI bugs.")
            ),
            ListPreference(
                "revanced_spoof_app_version_target",
                StringResource(
                    "revanced_spoof_app_version_target_title",
                    "Spoof app version target"
                ),
                ArrayResource(
                    "revanced_spoof_app_version_target_entries",
                    listOf(
                        StringResource("revanced_spoof_app_version_target_entry_1", "18.33.40 - Restore RYD Shorts incognito mode"),
                        StringResource("revanced_spoof_app_version_target_entry_2", "18.20.39 - Restore wide video speed & quality menu"),
                        StringResource("revanced_spoof_app_version_target_entry_3", "17.08.35 - Restore old UI layout"),
                        StringResource("revanced_spoof_app_version_target_entry_4", "16.08.35 - Restore explore tab"),
                        StringResource("revanced_spoof_app_version_target_entry_5", "16.01.35 - Restore fewer video player action buttons"),
                    )
                ),
                ArrayResource(
                    "revanced_spoof_app_version_target_entry_values",
                    listOf(
                        StringResource("revanced_spoof_app_version_target_entry_value_1", "18.33.40"),
                        StringResource("revanced_spoof_app_version_target_entry_value_2", "18.20.39"),
                        StringResource("revanced_spoof_app_version_target_entry_value_3", "17.08.35"),
                        StringResource("revanced_spoof_app_version_target_entry_value_4", "16.08.35"),
                        StringResource("revanced_spoof_app_version_target_entry_value_5", "16.01.35"),
                    )
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
