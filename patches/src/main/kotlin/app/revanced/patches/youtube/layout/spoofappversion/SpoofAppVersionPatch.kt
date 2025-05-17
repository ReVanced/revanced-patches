package app.revanced.patches.youtube.layout.spoofappversion

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceCategory
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_43_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/spoof/SpoofAppVersionPatch;"

val spoofAppVersionPatch = bytecodePatch(
    name = "Spoof app version",
    description = "Adds an option to trick YouTube into thinking you are running an older version of the app. " +
            "This can be used to restore old UI elements and features."
) {
    dependsOn(
        resourceMappingPatch,
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
        versionCheckPatch
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
            "19.47.53",
            "20.07.39",
            "20.12.46",
        )
    )

    execute {
        addResources("youtube", "layout.spoofappversion.spoofAppVersionPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            // Group the switch and list preference together, since General menu is sorted by name
            // and the preferences can be scattered apart with non English languages.
            PreferenceCategory(
                titleKey = null,
                sorting = Sorting.UNSORTED,
                tag = "app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory",
                preferences = setOf(
                    SwitchPreference("revanced_spoof_app_version"),
                    if (is_19_43_or_greater) {
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
            )
        )

        /**
         * If spoofing to target 19.20 or earlier the Library tab can crash due to
         * missing image resources. As a workaround, do not set an image in the
         * toolbar when the enum name is UNKNOWN.
         */
        toolBarButtonFingerprint.apply {
            val imageResourceIndex = instructionMatches[2].index
            val register = method.getInstruction<OneRegisterInstruction>(imageResourceIndex).registerA
            val jumpIndex = instructionMatches.last().index + 1

            method.addInstructionsWithLabels(
                imageResourceIndex + 1,
                "if-eqz v$register, :ignore",
                ExternalLabel("ignore", method.getInstruction(jumpIndex))
            )
        }

        spoofAppVersionFingerprint.apply {
            val index = instructionMatches.first().index
            val register = method.getInstruction<OneRegisterInstruction>(index).registerA

            method.addInstructions(
                index + 1,
                """
                    invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getYouTubeVersionOverride(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$register
                """
            )
        }
    }
}
