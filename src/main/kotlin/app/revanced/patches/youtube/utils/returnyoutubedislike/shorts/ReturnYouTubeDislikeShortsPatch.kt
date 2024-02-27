package app.revanced.patches.youtube.utils.returnyoutubedislike.shorts

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.utils.integrations.Constants.UTILS_PATH
import app.revanced.patches.youtube.utils.returnyoutubedislike.shorts.fingerprints.IncognitoFingerprint
import app.revanced.patches.youtube.utils.returnyoutubedislike.shorts.fingerprints.ShortsTextViewFingerprint
import app.revanced.patches.youtube.utils.returnyoutubedislike.shorts.fingerprints.TextComponentSpecFingerprint
import app.revanced.patches.youtube.utils.settings.SettingsPatch
import app.revanced.util.exception
import app.revanced.util.getTargetIndexReversed
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch(dependencies = [SettingsPatch::class])
object ReturnYouTubeDislikeShortsPatch : BytecodePatch(
    setOf(
        IncognitoFingerprint,
        ShortsTextViewFingerprint,
        TextComponentSpecFingerprint
    )
) {
    private const val INTEGRATIONS_RYD_CLASS_DESCRIPTOR =
        "$UTILS_PATH/ReturnYouTubeDislikePatch;"

    override fun execute(context: BytecodeContext) {
        ShortsTextViewFingerprint.result?.let {
            it.mutableMethod.apply {
                val startIndex = it.scanResult.patternScanResult!!.startIndex

                val isDisLikesBooleanIndex = getTargetIndexReversed(startIndex, Opcode.IGET_BOOLEAN)
                val textViewFieldIndex = getTargetIndexReversed(startIndex, Opcode.IGET_OBJECT)

                // If the field is true, the TextView is for a dislike button.
                val isDisLikesBooleanReference =
                    getInstruction<ReferenceInstruction>(isDisLikesBooleanIndex).reference

                val textViewFieldReference = // Like/Dislike button TextView field
                    getInstruction<ReferenceInstruction>(textViewFieldIndex).reference

                // Check if the hooked TextView object is that of the dislike button.
                // If RYD is disabled, or the TextView object is not that of the dislike button, the execution flow is not interrupted.
                // Otherwise, the TextView object is modified, and the execution flow is interrupted to prevent it from being changed afterward.
                val insertIndex = implementation!!.instructions.indexOfFirst { instruction ->
                    instruction.opcode == Opcode.CHECK_CAST
                } + 1

                addInstructionsWithLabels(
                    insertIndex, """
                    # Check, if the TextView is for a dislike button
                    iget-boolean v0, p0, $isDisLikesBooleanReference
                    if-eqz v0, :ryd_disabled
                    
                    # Hook the TextView, if it is for the dislike button
                    iget-object v0, p0, $textViewFieldReference
                    invoke-static {v0}, $INTEGRATIONS_RYD_CLASS_DESCRIPTOR->setShortsDislikes(Landroid/view/View;)Z
                    move-result v0
                    if-eqz v0, :ryd_disabled
                    return-void
                    """, ExternalLabel("ryd_disabled", getInstruction(insertIndex))
                )
            }
        } ?: throw ShortsTextViewFingerprint.exception

        if (SettingsPatch.upward1834) {
            TextComponentSpecFingerprint.result?.let {
                it.mutableMethod.apply {
                    val insertIndex = it.scanResult.patternScanResult!!.startIndex

                    val charSequenceRegister =
                        getInstruction<FiveRegisterInstruction>(insertIndex).registerC
                    val conversionContextRegister =
                        getInstruction<TwoRegisterInstruction>(0).registerA

                    val replaceReference =
                        getInstruction<ReferenceInstruction>(insertIndex).reference

                    addInstructions(
                        insertIndex + 1, """
                            invoke-static {v$conversionContextRegister, v$charSequenceRegister}, $INTEGRATIONS_RYD_CLASS_DESCRIPTOR->onCharSequenceLoaded(Ljava/lang/Object;Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                            move-result-object v$charSequenceRegister
                            invoke-static {v$charSequenceRegister}, $replaceReference
                            """
                    )
                    removeInstruction(insertIndex)
                }
            } ?: throw TextComponentSpecFingerprint.exception

            IncognitoFingerprint.result?.let {
                it.mutableMethod.apply {
                    addInstruction(
                        1,
                        "sput-boolean p4, $INTEGRATIONS_RYD_CLASS_DESCRIPTOR->isIncognito:Z"
                    )
                }
            } ?: throw IncognitoFingerprint.exception
        }
    }
}
