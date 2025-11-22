package app.revanced.patches.youtube.layout.startpage

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceCategory
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/ChangeStartPagePatch;"

val changeStartPagePatch = bytecodePatch(
    name = "Change start page",
    description = "Adds an option to set which page the app opens in instead of the homepage.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "19.43.41",
            "20.14.43",
            "20.21.37",
            "20.31.40",
        )
    )

    execute {
        addResources("youtube", "layout.startpage.changeStartPagePatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            PreferenceCategory(
                titleKey = null,
                sorting = Sorting.UNSORTED,
                tag = "app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory",
                preferences = setOf(
                    ListPreference(
                        key = "revanced_change_start_page",
                        tag = "app.revanced.extension.shared.settings.preference.SortedListPreference"
                    ),
                    SwitchPreference("revanced_change_start_page_always")
                )
            )
        )

        // Hook browseId.
        browseIdFingerprint.let {
            it.method.apply {
                val browseIdIndex = it.instructionMatches.first().index
                val browseIdRegister = getInstruction<OneRegisterInstruction>(browseIdIndex).registerA

                addInstructions(
                    browseIdIndex + 1,
                    """
                        invoke-static { v$browseIdRegister }, $EXTENSION_CLASS_DESCRIPTOR->overrideBrowseId(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$browseIdRegister
                    """
                )
            }
        }

        // There is no browserId assigned to Shorts and Search.
        // Just hook the Intent action.
        intentActionFingerprint.method.addInstruction(
            0,
            "invoke-static { p1 }, $EXTENSION_CLASS_DESCRIPTOR->overrideIntentAction(Landroid/content/Intent;)V",
        )
    }
}
