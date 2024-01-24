package app.revanced.patches.youtube.interaction.dialog

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.interaction.dialog.fingerprints.CreateDialogFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch(
    name = "Remove viewer discretion dialog",
    description = "Adds an option to remove the dialog that appears when opening a video that has been age-restricted " +
            "by accepting it automatically. This does not bypass the age restriction.",
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
                "19.01.34",
                "19.02.39",
                "19.03.35"
            ]
        )
    ]
)
@Suppress("unused")
object RemoveViewerDiscretionDialogPatch : BytecodePatch(
    setOf(CreateDialogFingerprint)
) {
    private const val INTEGRATIONS_METHOD_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/RemoveViewerDiscretionDialogPatch;->" +
                "confirmDialog(Landroid/app/AlertDialog;)V"

    override fun execute(context: BytecodeContext) {
        SettingsPatch.PreferenceScreen.INTERACTIONS.addPreferences(
            SwitchPreference(
                "revanced_remove_viewer_discretion_dialog",
                StringResource(
                    "revanced_remove_viewer_discretion_dialog_title",
                    "Remove viewer discretion dialog"
                ),
                StringResource(
                    "revanced_remove_viewer_discretion_dialog_summary_on",
                    "Dialog will be removed"
                ),
                StringResource(
                    "revanced_remove_viewer_discretion_dialog_summary_off",
                    "Dialog will be shown"
                ),
                StringResource(
                    "revanced_remove_viewer_discretion_dialog_user_dialog_message",
                    "This does not bypass the age restriction. It just accepts it automatically."
                )
            )
        )

        CreateDialogFingerprint.result?.mutableMethod?.apply {
            val showDialogIndex = implementation!!.instructions.lastIndex - 2
            val dialogRegister = getInstruction<FiveRegisterInstruction>(showDialogIndex).registerC

            replaceInstructions(
                showDialogIndex,
                "invoke-static { v$dialogRegister }, $INTEGRATIONS_METHOD_DESCRIPTOR",
            )
        } ?: throw CreateDialogFingerprint.exception
    }
}
