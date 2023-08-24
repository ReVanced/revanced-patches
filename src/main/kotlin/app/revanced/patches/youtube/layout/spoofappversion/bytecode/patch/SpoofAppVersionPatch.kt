package app.revanced.patches.youtube.layout.spoofappversion.bytecode.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.ArrayResource
import app.revanced.patches.shared.settings.preference.impl.ListPreference
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.layout.spoofappversion.annotations.SpoofAppVersionCompatibility
import app.revanced.patches.youtube.layout.spoofappversion.bytecode.fingerprints.SpoofAppVersionFingerprint
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Name("Spoof app version")
@Description("Tricks YouTube into thinking you are running an older version of the app. One of the side effects also includes restoring the old UI.")
@SpoofAppVersionCompatibility
class SpoofAppVersionPatch : BytecodePatch(
    listOf(
        SpoofAppVersionFingerprint
    )
) {
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
                        + "\\n\\nIf later turned off, the old UI may remain until the app data is cleared.")
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
                        StringResource("revanced_spoof_app_version_target_entry_1", "17.08.35 - Restore old UI layout"),
                        StringResource("revanced_spoof_app_version_target_entry_2", "16.08.35 - Restore explore tab"),
                        StringResource("revanced_spoof_app_version_target_entry_3", "16.01.35 - Restore old Shorts player"),
                    )
                ),
                ArrayResource(
                    "revanced_spoof_app_version_target_entry_values",
                    listOf(
                        StringResource("revanced_spoof_app_version_target_entry_value_1", "17.08.35"),
                        StringResource("revanced_spoof_app_version_target_entry_value_2", "16.08.35"),
                        StringResource("revanced_spoof_app_version_target_entry_value_3", "16.01.35"),
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
                    invoke-static {v$buildOverrideNameRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR;->getYouTubeVersionOverride(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$buildOverrideNameRegister
                """
            )
        } ?: throw SpoofAppVersionFingerprint.exception
    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/SpoofAppVersionPatch"
    }
}
