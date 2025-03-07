package app.revanced.patches.youtube.layout.spoofappversion

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceCategory
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playservice.is_19_17_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal var menuItemView = -1L
    private set

internal val spoofAppVersionResourcePatch = resourcePatch {
    dependsOn(
        resourceMappingPatch
    )

    execute {
        menuItemView =  resourceMappings["id", "menu_item_view"]
    }
}

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/spoof/SpoofAppVersionPatch;"

val spoofAppVersionPatch = bytecodePatch(
    name = "Spoof app version",
    description = "Adds an option to trick YouTube into thinking you are running an older version of the app. " +
            "This can be used to restore old UI elements and features. " +
            "Patching 19.16.39 includes additional older spoofing targets.",
) {
    dependsOn(
        spoofAppVersionResourcePatch,
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
            // Group the switch and list preference together, since General menu is sorted by name
            // and the preferences can be scattered apart with non English langauges.
            PreferenceCategory(
                titleKey = null,
                sorting = Sorting.UNSORTED,
                tag = "app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory",
                preferences = setOf(
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
            )
        )

        /**
         * If a user really wants to spoof to very old versions with the latest app target
         * they can  modify the import/export spoof version.  But when spoofing the 19.20.xx
         * or earlier the Library tab can crash due to missing image resources trying to load.
         * As a temporary workaround, do not set an image in the toolbar when the enum name is UNKNOWN.
         */
        toolBarButtonFingerprint.method.apply {
            val getDrawableIndex = indexOfGetDrawableInstruction(this)
            val enumOrdinalIndex = indexOfFirstInstructionReversedOrThrow(getDrawableIndex) {
                opcode == Opcode.INVOKE_INTERFACE &&
                        getReference<MethodReference>()?.returnType == "I"
            }
            val insertIndex = enumOrdinalIndex + 2
            val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA
            val jumpIndex = indexOfFirstInstructionOrThrow(insertIndex) {
                opcode == Opcode.INVOKE_VIRTUAL &&
                        getReference<MethodReference>()?.name == "setImageDrawable"
            } + 1

            addInstructionsWithLabels(
                insertIndex,
                "if-eqz v$insertRegister, :ignore",
                ExternalLabel("ignore", getInstruction(jumpIndex))
            )
        }

        val insertIndex = spoofAppVersionFingerprint.patternMatch!!.startIndex + 1
        val buildOverrideNameRegister =
            spoofAppVersionFingerprint.method.getInstruction<OneRegisterInstruction>(insertIndex - 1).registerA

        spoofAppVersionFingerprint.method.addInstructions(
            insertIndex,
            """
                invoke-static {v$buildOverrideNameRegister}, $EXTENSION_CLASS_DESCRIPTOR->getYouTubeVersionOverride(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v$buildOverrideNameRegister
            """
        )
    }
}
