package app.revanced.patches.youtube.interaction.dialog

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Suppress("unused")
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
            "18.38.44",
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
        ),
    )

    val createDialogMatch by createDialogFingerprint()

    val extensionMethodDescriptor =
        "Lapp/revanced/extension/youtube/patches/RemoveViewerDiscretionDialogPatch;->" +
            "confirmDialog(Landroid/app/AlertDialog;)V"

    execute {
        addResources("youtube", "interaction.dialog.removeViewerDiscretionDialogPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_remove_viewer_discretion_dialog"),
        )

        createDialogMatch.mutableMethod.apply {
            val showDialogIndex = implementation!!.instructions.lastIndex - 2
            val dialogRegister = getInstruction<FiveRegisterInstruction>(showDialogIndex).registerC

            replaceInstructions(
                showDialogIndex,
                "invoke-static { v$dialogRegister }, $extensionMethodDescriptor",
            )
        }
    }
}