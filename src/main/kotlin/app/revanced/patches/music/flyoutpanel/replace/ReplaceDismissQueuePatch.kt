package app.revanced.patches.music.flyoutpanel.replace

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.flyoutpanel.utils.EnumUtils.getEnumIndex
import app.revanced.patches.music.utils.fingerprints.MenuItemFingerprint
import app.revanced.patches.music.utils.integrations.Constants.FLYOUT
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.patches.music.video.information.VideoInformationPatch
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Replace dismiss queue",
    description = "Adds an option to replace \"Dismiss queue\" with \"Watch on YouTube\" in the flyout menu.",
    dependencies = [
        SettingsPatch::class,
        VideoInformationPatch::class
    ],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object ReplaceDismissQueuePatch : BytecodePatch(
    setOf(MenuItemFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        MenuItemFingerprint.result?.let {
            it.mutableMethod.apply {
                val enumIndex = getEnumIndex()
                val enumRegister = getInstruction<OneRegisterInstruction>(enumIndex).registerA

                val textViewIndex = it.scanResult.patternScanResult!!.startIndex
                val imageViewIndex = it.scanResult.patternScanResult!!.endIndex

                val textViewRegister =
                    getInstruction<OneRegisterInstruction>(textViewIndex).registerA
                val imageViewRegister =
                    getInstruction<OneRegisterInstruction>(imageViewIndex).registerA

                addInstruction(
                    enumIndex + 1,
                    "invoke-static {v$enumRegister, v$textViewRegister, v$imageViewRegister}, $FLYOUT->replaceDismissQueue(Ljava/lang/Enum;Landroid/widget/TextView;Landroid/widget/ImageView;)V"
                )
            }
        } ?: throw MenuItemFingerprint.exception

        SettingsPatch.addMusicPreference(
            CategoryType.FLYOUT,
            "revanced_replace_flyout_panel_dismiss_queue",
            "false"
        )

        SettingsPatch.addMusicPreference(
            CategoryType.FLYOUT,
            "revanced_replace_flyout_panel_dismiss_queue_continue_watch",
            "true",
            "revanced_replace_flyout_panel_dismiss_queue"
        )

    }
}
