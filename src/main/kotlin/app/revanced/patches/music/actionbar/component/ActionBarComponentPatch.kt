package app.revanced.patches.music.actionbar.component

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.music.actionbar.component.fingerprints.ActionBarComponentFingerprint
import app.revanced.patches.music.actionbar.component.fingerprints.LikeDislikeContainerFingerprint
import app.revanced.patches.music.actionbar.component.fingerprints.LikeDislikeContainerVisibilityFingerprint
import app.revanced.patches.music.utils.integrations.Constants.ACTIONBAR
import app.revanced.patches.music.utils.intenthook.IntentHookPatch
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.music.utils.resourceid.SharedResourceIdPatch.LikeDislikeContainer
import app.revanced.patches.music.utils.settings.CategoryType
import app.revanced.patches.music.utils.settings.SettingsPatch
import app.revanced.patches.music.video.information.VideoInformationPatch
import app.revanced.util.exception
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import kotlin.math.min

@Patch(
    name = "Hide action bar component",
    description = "Adds options to hide action bar components and replace the offline download button with an external download button.",
    dependencies = [
        IntentHookPatch::class,
        SettingsPatch::class,
        SharedResourceIdPatch::class,
        VideoInformationPatch::class
    ],
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object ActionBarComponentPatch : BytecodePatch(
    setOf(
        ActionBarComponentFingerprint,
        LikeDislikeContainerFingerprint
    )
) {
    private var spannedReference = ""

    override fun execute(context: BytecodeContext) {
        ActionBarComponentFingerprint.result?.let {
            it.mutableMethod.apply {
                val instructions = implementation!!.instructions

                // hook download button
                val addViewIndex = instructions.indexOfLast { instruction ->
                    ((instruction as? ReferenceInstruction)?.reference as? MethodReference)?.name == "addView"
                }
                val addViewRegister = getInstruction<FiveRegisterInstruction>(addViewIndex).registerD

                addInstruction(
                    addViewIndex + 1,
                    "invoke-static {v$addViewRegister}, $ACTIONBAR->hookDownloadButton(Landroid/view/View;)V"
                )

                // hide action button label
                val noLabelIndex = instructions.indexOfFirst { instruction ->
                    val reference = (instruction as? ReferenceInstruction)?.reference.toString()
                    instruction.opcode == Opcode.INVOKE_DIRECT
                            && reference.endsWith("<init>(Landroid/content/Context;)V")
                            && !reference.contains("Lcom/google/android/libraries/youtube/common/ui/YouTubeButton;")
                } - 2

                val replaceIndex = instructions.indexOfFirst { instruction ->
                    val reference = (instruction as? ReferenceInstruction)?.reference.toString()
                    instruction.opcode == Opcode.INVOKE_DIRECT
                            && reference.endsWith("Lcom/google/android/libraries/youtube/common/ui/YouTubeButton;-><init>(Landroid/content/Context;)V")
                } - 2
                val replaceInstruction = getInstruction<TwoRegisterInstruction>(replaceIndex)
                val replaceReference = getInstruction<ReferenceInstruction>(replaceIndex).reference

                addInstructionsWithLabels(
                    replaceIndex + 1, """
                        invoke-static {}, $ACTIONBAR->hideActionBarLabel()Z
                        move-result v${replaceInstruction.registerA}
                        if-nez v${replaceInstruction.registerA}, :hidden
                        iget-object v${replaceInstruction.registerA}, v${replaceInstruction.registerB}, $replaceReference
                        """, ExternalLabel("hidden", getInstruction(noLabelIndex))
                )
                removeInstruction(replaceIndex)

                // hide action button
                val hasNextIndex = instructions.indexOfFirst { instruction ->
                    ((instruction as? ReferenceInstruction)?.reference as? MethodReference)?.name == "hasNext"
                }

                val freeRegister = min(implementation!!.registerCount - parameters.size - 2, 15)

                val spannedIndex = instructions.indexOfFirst { instruction ->
                    spannedReference = (instruction as? ReferenceInstruction)?.reference.toString()
                    spannedReference.endsWith("Landroid/text/Spanned;")
                }
                val spannedRegister = getInstruction<FiveRegisterInstruction>(spannedIndex).registerC

                addInstructionsWithLabels(
                    spannedIndex + 1, """
                        invoke-static {}, $ACTIONBAR->hideActionButton()Z
                        move-result v$freeRegister
                        if-nez v$freeRegister, :hidden
                        invoke-static {v$spannedRegister}, $spannedReference
                        """, ExternalLabel("hidden", getInstruction(hasNextIndex))
                )
                removeInstruction(spannedIndex)

                // set action button identifier
                val buttonTypeDownloadIndex = it.scanResult.patternScanResult!!.startIndex + 1
                val buttonTypeDownloadRegister = getInstruction<OneRegisterInstruction>(buttonTypeDownloadIndex).registerA

                val buttonTypeIndex = it.scanResult.patternScanResult!!.endIndex - 1
                val buttonTypeRegister = getInstruction<OneRegisterInstruction>(buttonTypeIndex).registerA

                addInstruction(
                    buttonTypeIndex + 2,
                    "invoke-static {v$buttonTypeRegister}, $ACTIONBAR->setButtonType(Ljava/lang/Object;)V"
                )

                addInstruction(
                    buttonTypeDownloadIndex,
                    "invoke-static {v$buttonTypeDownloadRegister}, $ACTIONBAR->setButtonTypeDownload(I)V"
                )
            }
        } ?: throw ActionBarComponentFingerprint.exception

        LikeDislikeContainerFingerprint.result?.let { parentResult ->
            /**
             * Added in YouTube Music v6.35.xx~
             */
            LikeDislikeContainerVisibilityFingerprint.also { it.resolve(context, parentResult.classDef) }.result?.let {
                it.mutableMethod.apply {
                    val targetIndex = it.scanResult.patternScanResult!!.startIndex + 1
                    val targetRegister =
                        getInstruction<OneRegisterInstruction>(targetIndex).registerA

                    addInstructions(
                        targetIndex + 1, """
                            invoke-static {v$targetRegister}, $ACTIONBAR->hideLikeDislikeButton(Z)Z
                            move-result v$targetRegister
                            """
                    )
                }
            } // Don't throw exception

            parentResult.mutableMethod.apply {
                val insertIndex = getWideLiteralInstructionIndex(LikeDislikeContainer) + 2
                val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstruction(
                    insertIndex + 1,
                    "invoke-static {v$insertRegister}, $ACTIONBAR->hideLikeDislikeButton(Landroid/view/View;)V"
                )
            }
        } ?: throw LikeDislikeContainerFingerprint.exception

        SettingsPatch.addMusicPreference(
            CategoryType.ACTION_BAR,
            "revanced_hide_action_button_add_to_playlist",
            "false"
        )
        SettingsPatch.addMusicPreference(
            CategoryType.ACTION_BAR,
            "revanced_hide_action_button_comment",
            "false"
        )
        SettingsPatch.addMusicPreference(
            CategoryType.ACTION_BAR,
            "revanced_hide_action_button_download",
            "false"
        )
        SettingsPatch.addMusicPreference(
            CategoryType.ACTION_BAR,
            "revanced_hide_action_button_label",
            "false"
        )
        SettingsPatch.addMusicPreference(
            CategoryType.ACTION_BAR,
            "revanced_hide_action_button_like_dislike",
            "false"
        )
        SettingsPatch.addMusicPreference(
            CategoryType.ACTION_BAR,
            "revanced_hide_action_button_radio",
            "false"
        )
        SettingsPatch.addMusicPreference(
            CategoryType.ACTION_BAR,
            "revanced_hide_action_button_share",
            "false"
        )
        SettingsPatch.addMusicPreference(
            CategoryType.ACTION_BAR,
            "revanced_hook_action_button_download",
            "false"
        )
        SettingsPatch.addMusicPreferenceWithIntent(
            CategoryType.ACTION_BAR,
            "revanced_external_downloader_package_name",
            "revanced_hook_action_button_download"
        )

    }
}