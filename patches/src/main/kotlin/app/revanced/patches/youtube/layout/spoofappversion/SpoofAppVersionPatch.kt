package app.revanced.patches.youtube.layout.spoofappversion

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_17_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/spoof/SpoofAppVersionPatch;"

val spoofAppVersionPatch = bytecodePatch(
    name = "Spoof app version",
    description = "Adds an option to trick YouTube into thinking you are running an older version of the app. " +
            "This can be used to restore old UI elements and features. " +
            "Patching 19.16.39 or lower includes additional older spoofing targets.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        versionCheckPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.38.44",
            "18.49.37",
            "19.16.39",
            // "19.25.37", // Cannot be supported because the lowest spoof target is higher.
            // "19.34.42", // Cannot be supported because the lowest spoof target is higher.
            "19.43.41",
            "19.45.38",
            "19.46.42",
            "19.47.53",
        ),
    )

    execute {
        addResources("youtube", "layout.spoofappversion.spoofAppVersionPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_spoof_app_version"),
            if (is_19_17_or_greater) {
                ListPreference(
                    key = "revanced_spoof_app_version_target",
                    summaryKey = null,
                )
            } else {
                ListPreference(
                    key = "revanced_spoof_app_version_target",
                    summaryKey = null,
                    entriesKey = "revanced_spoof_app_version_target_legacy_entries",
                    entryValuesKey = "revanced_spoof_app_version_target_legacy_entry_values"
                )
            }
        )

        val insertIndex = spoofAppVersionFingerprint.filterMatches.first().index + 1
        val buildOverrideNameRegister =
            spoofAppVersionFingerprint.method.getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA

        spoofAppVersionFingerprint.method.addInstructions(
            insertIndex,
            """
                invoke-static {v$buildOverrideNameRegister}, $EXTENSION_CLASS_DESCRIPTOR->getYouTubeVersionOverride(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v$buildOverrideNameRegister
            """,
        )
    }
}
