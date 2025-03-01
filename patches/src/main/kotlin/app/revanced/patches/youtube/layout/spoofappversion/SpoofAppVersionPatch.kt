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
            "Patching 19.16.39 includes additional older spoofing targets.",
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

        /**
         * Shorts player is broken when spoofing to very old versions.
         * But if a user still really wants to they can modify the import/export spoof version.
         * But when spoofing the 19.20.xx or earlier the Library tab can crash due to missing
         * image resources trying to load. As a temporary workaround, do not set an image
         * in the toolbar when the enum name is UNKNOWN.
         */
        toolBarButtonFingerprint.let {
            it.method.apply {
                val imageResourceIndex = it.instructionMatches[2].index
                val register = getInstruction<OneRegisterInstruction>(imageResourceIndex).registerA
                val jumpIndex = it.instructionMatches.last().index + 1

                addInstructionsWithLabels(
                    imageResourceIndex + 1,
                    "if-eqz v$register, :ignore",
                    ExternalLabel("ignore", getInstruction(jumpIndex))
                )
            }
        }

        spoofAppVersionFingerprint.let {
            it.method.apply {
                val index = it.instructionMatches.first().index
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                addInstructions(
                    index + 1,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->getYouTubeVersionOverride(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$register
                    """
                )
            }
        }
    }
}
