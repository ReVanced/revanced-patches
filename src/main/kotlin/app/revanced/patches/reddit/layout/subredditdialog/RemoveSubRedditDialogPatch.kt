package app.revanced.patches.reddit.layout.subredditdialog

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.layout.subredditdialog.fingerprints.FrequentUpdatesSheetScreenFingerprint
import app.revanced.patches.reddit.layout.subredditdialog.fingerprints.RedditAlertDialogsFingerprint
import app.revanced.patches.reddit.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.reddit.utils.resourceid.SharedResourceIdPatch.CancelButton
import app.revanced.patches.reddit.utils.resourceid.SharedResourceIdPatch.TextAppearanceRedditBaseOldButtonColored
import app.revanced.patches.reddit.utils.settings.SettingsBytecodePatch.updateSettingsStatus
import app.revanced.patches.reddit.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Remove subreddit dialog",
    description = "Adds options to remove the NSFW community warning and notifications suggestion dialogs by dismissing them automatically.",
    dependencies = [SettingsPatch::class, SharedResourceIdPatch::class],
    compatiblePackages = [CompatiblePackage("com.reddit.frontpage")],
)
@Suppress("unused")
object RemoveSubRedditDialogPatch : BytecodePatch(
    setOf(
        FrequentUpdatesSheetScreenFingerprint,
        RedditAlertDialogsFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/reddit/patches/RemoveSubRedditDialogPatch;"

    override fun execute(context: BytecodeContext) {

        FrequentUpdatesSheetScreenFingerprint.result?.let {
            it.mutableMethod.apply {
                val cancelButtonViewIndex = getWideLiteralInstructionIndex(CancelButton) + 2
                val cancelButtonViewRegister = getInstruction<OneRegisterInstruction>(cancelButtonViewIndex).registerA

                addInstruction(
                    cancelButtonViewIndex + 1,
                    "invoke-static {v$cancelButtonViewRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->dismissDialog(Landroid/view/View;)V"
                )
            }
        } ?: throw FrequentUpdatesSheetScreenFingerprint.exception

        RedditAlertDialogsFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = getWideLiteralInstructionIndex(TextAppearanceRedditBaseOldButtonColored) + 1
                val insertRegister = getInstruction<FiveRegisterInstruction>(insertIndex).registerC

                addInstruction(
                    insertIndex,
                    "invoke-static {v$insertRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->confirmDialog(Landroid/widget/TextView;)V"
                )
            }
        } ?: throw RedditAlertDialogsFingerprint.exception

        updateSettingsStatus("RemoveSubRedditDialog")

    }
}
