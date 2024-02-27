package app.revanced.patches.youtube.buttomplayer.comment

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.buttomplayer.comment.fingerprints.ShortsLiveStreamEmojiPickerOnClickListenerFingerprint
import app.revanced.patches.youtube.buttomplayer.comment.fingerprints.ShortsLiveStreamEmojiPickerOpacityFingerprint
import app.revanced.patches.youtube.buttomplayer.comment.fingerprints.ShortsLiveStreamThanksFingerprint
import app.revanced.patches.youtube.utils.integrations.Constants.BOTTOM_PLAYER
import app.revanced.patches.youtube.utils.integrations.Constants.COMPONENTS_PATH
import app.revanced.patches.youtube.utils.litho.LithoFilterPatch
import app.revanced.patches.youtube.utils.resourceid.SharedResourceIdPatch
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getTargetIndex
import app.revanced.util.getWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Hide comment component",
    description = "Adds options to hide components related to comments.",
    dependencies = [
        LithoFilterPatch::class,
        SettingsPatch::class,
        SharedResourceIdPatch::class
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.25.40",
                "18.27.36",
                "18.29.38",
                "18.30.37",
                "18.31.40",
                "18.32.39",
                "18.33.40",
                "18.34.38",
                "18.35.36",
                "18.36.39",
                "18.37.36",
                "18.38.44",
                "18.39.41",
                "18.40.34",
                "18.41.39",
                "18.42.41",
                "18.43.45",
                "18.44.41",
                "18.45.43",
                "18.46.45",
                "18.48.39",
                "18.49.37",
                "19.01.34",
                "19.02.39"
            ]
        )
    ]
)
@Suppress("unused")
object CommentComponentPatch : BytecodePatch(
    setOf(
        ShortsLiveStreamEmojiPickerOnClickListenerFingerprint,
        ShortsLiveStreamEmojiPickerOpacityFingerprint
    )
) {
    override fun execute(context: BytecodeContext) {

        ShortsLiveStreamEmojiPickerOpacityFingerprint.result?.let {
            it.mutableMethod.apply {
                val insertIndex = implementation!!.instructions.size - 1
                val insertRegister= getInstruction<OneRegisterInstruction>(insertIndex).registerA

                addInstruction(
                    insertIndex,
                    "invoke-static {v$insertRegister}, $BOTTOM_PLAYER->changeEmojiPickerOpacity(Landroid/widget/ImageView;)V"
                )
            }
        } ?: throw ShortsLiveStreamEmojiPickerOpacityFingerprint.exception

        ShortsLiveStreamEmojiPickerOnClickListenerFingerprint.result?.let { parentResult ->
            parentResult.mutableMethod.apply {
                val emojiPickerEndpointIndex = getWideLiteralInstructionIndex(126326492)
                val emojiPickerOnClickListenerIndex = getTargetIndex(emojiPickerEndpointIndex, Opcode.INVOKE_DIRECT)
                val emojiPickerOnClickListenerMethod =
                    context.toMethodWalker(this)
                        .nextMethod(emojiPickerOnClickListenerIndex, true)
                        .getMethod() as MutableMethod

                emojiPickerOnClickListenerMethod.apply {
                    val insertIndex = implementation!!.instructions.indexOfFirst { instruction ->
                        instruction.opcode == Opcode.IF_EQZ
                    }
                    val insertRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                    addInstructions(
                        insertIndex, """
                            invoke-static {v$insertRegister}, $BOTTOM_PLAYER->disableEmojiPickerOnClickListener(Ljava/lang/Object;)Ljava/lang/Object;
                            move-result-object v$insertRegister
                            """
                    )
                }
            }

            ShortsLiveStreamThanksFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.let {
                it.mutableMethod.apply {
                    val insertIndex = it.scanResult.patternScanResult!!.startIndex
                    val insertInstruction = getInstruction<FiveRegisterInstruction>(insertIndex)

                    addInstructions(
                        insertIndex,"""
                            invoke-static { v${insertInstruction.registerC}, v${insertInstruction.registerD} }, $BOTTOM_PLAYER->hideThanksButton(Landroid/view/View;I)I
                            move-result v${insertInstruction.registerD}
                            """
                    )
                }
            }
        }

        LithoFilterPatch.addFilter("$COMPONENTS_PATH/CommentsFilter;")

        /**
         * Add settings
         */
        SettingsPatch.addPreference(
            arrayOf(
                "PREFERENCE: BOTTOM_PLAYER_SETTINGS",
                "SETTINGS: COMMENT_COMPONENTS"
            )
        )

        SettingsPatch.updatePatchStatus("Hide comment component")

    }
}
