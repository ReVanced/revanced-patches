package app.revanced.patches.youtube.interaction.dialog

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.interaction.dialog.fingerprints.CreateDialogFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

@Patch(
    name = "Remove viewer discretion dialog",
    description = "Adds an option to remove the dialog that appears when opening a video that has been age-restricted " +
            "by accepting it automatically. This does not bypass the age restriction.",
    dependencies = [IntegrationsPatch::class, SettingsPatch::class, AddResourcesPatch::class],
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
                "19.03.36",
                "19.04.38",
                "19.05.36",
                "19.06.39",
                "19.07.40",
                "19.08.36",
                "19.09.38",
                "19.10.39",
                "19.11.43"
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
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            SwitchPreference("revanced_remove_viewer_discretion_dialog")
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
