package app.revanced.patches.music.layout.startpage

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.fieldReference
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.instructions
import app.revanced.patcher.extensions.string
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.music.misc.extension.sharedExtensionPatch
import app.revanced.patches.music.misc.settings.PreferenceScreen
import app.revanced.patches.music.misc.settings.settingsPatch
import app.revanced.patches.music.shared.mainActivityOnCreateMethod
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceCategory
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.indexOfFirstInstructionReversed
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/music/patches/ChangeStartPagePatch;"

val changeStartPagePatch = bytecodePatch(
    name = "Change start page",
    description = "Adds an option to set which page the app opens in instead of the homepage.",
) {
    dependsOn(
        sharedExtensionPatch,
        settingsPatch,
        addResourcesPatch
    )

    compatibleWith(
        "com.google.android.apps.youtube.music"(
            "7.29.52",
            "8.10.52",
            "8.37.56",
            "8.40.54",
        )
    )

    apply {
        addResources("music", "layout.startpage.changeStartPagePatch")

        PreferenceScreen.GENERAL.addPreferences(
            PreferenceCategory(
                titleKey = null,
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                tag = "app.revanced.extension.shared.settings.preference.NoTitlePreferenceCategory",
                preferences = setOf(
                    ListPreference(
                        key = "revanced_change_start_page",
                        tag = "app.revanced.extension.shared.settings.preference.SortedListPreference"
                    )
                )
            )
        )

        coldStartUpMethodMatch.let { match ->
            match.method.apply {
                val defaultBrowseIdIndex = match[-1]

                val browseIdIndex = indexOfFirstInstructionReversed(defaultBrowseIdIndex) {
                    opcode == Opcode.IGET_OBJECT && fieldReference?.type == "Ljava/lang/String;"
                }

                if (browseIdIndex != -1) {
                    val browseIdRegister =
                        getInstruction<TwoRegisterInstruction>(browseIdIndex).registerA
                    addInstructions(
                        browseIdIndex + 1,
                        """
                            invoke-static/range { v$browseIdRegister .. v$browseIdRegister }, $EXTENSION_CLASS_DESCRIPTOR->overrideBrowseId(Ljava/lang/String;)Ljava/lang/String;
                            move-result-object v$browseIdRegister
                        """
                    )
                } else {
                    instructions.mapIndexedNotNull { index, instr ->
                        if (instr.opcode == Opcode.RETURN_OBJECT) index else null
                    }.reversed().forEach { returnIndex ->
                        val returnRegister =
                            getInstruction<OneRegisterInstruction>(returnIndex).registerA

                        addInstructions(
                            returnIndex,
                            """
                                invoke-static/range { v$returnRegister .. v$returnRegister }, $EXTENSION_CLASS_DESCRIPTOR->overrideBrowseId(Ljava/lang/String;)Ljava/lang/String;
                                move-result-object v$returnRegister
                            """
                        )
                    }
                }
            }
        }

        mainActivityOnCreateMethod.apply {
            val p0 = implementation!!.registerCount - 2
            val p1 = p0 + 1

            addInstruction(
                0,
                "invoke-static/range { v$p0 .. v$p1 }, " +
                        "$EXTENSION_CLASS_DESCRIPTOR->" +
                        "overrideIntentActionOnCreate(Landroid/app/Activity;Landroid/os/Bundle;)V"
            )
        }
    }
}
