package app.revanced.patches.youtube.interaction.dialog

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/youtube/patches/RemoveViewerDiscretionDialogPatch;"

val removeViewerDiscretionDialogPatch = bytecodePatch(
    name = "Remove viewer discretion dialog",
    description = "Adds an option to remove the dialog that appears when opening a video that has been age-restricted " +
        "by accepting it automatically. This does not bypass the age restriction.",
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
        addResources("youtube", "interaction.dialog.removeViewerDiscretionDialogPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_remove_viewer_discretion_dialog"),
        )

        createDialogFingerprint.let {
            it.method.apply {
                val showDialogIndex = it.instructionMatches.last().index
                val dialogRegister = getInstruction<FiveRegisterInstruction>(showDialogIndex).registerC

                replaceInstructions(
                    showDialogIndex,
                    "invoke-static { v$dialogRegister }, $EXTENSION_CLASS_DESCRIPTOR->confirmDialog(Landroid/app/AlertDialog;)V",
                )
            }
        }
    }
}
