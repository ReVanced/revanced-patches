package app.revanced.patches.youtube.layout.returnyoutubedislike.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.extensions.MethodFingerprintExtensions.name
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultError
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.returnyoutubedislike.annotations.ReturnYouTubeDislikeCompatibility
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.*
import app.revanced.patches.youtube.layout.returnyoutubedislike.resource.patch.ReturnYouTubeDislikeResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.playertype.patch.PlayerTypeHookPatch
import app.revanced.patches.youtube.video.videoid.patch.VideoIdPatch
import org.jf.dexlib2.iface.instruction.FiveRegisterInstruction
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch
@DependsOn(
    [
        IntegrationsPatch::class,
        VideoIdPatch::class,
        ReturnYouTubeDislikeResourcePatch::class,
        PlayerTypeHookPatch::class,
    ]
)
@Name("return-youtube-dislike")
@Description("Shows the dislike count of videos using the Return YouTube Dislike API.")
@ReturnYouTubeDislikeCompatibility
@Version("0.0.1")
class ReturnYouTubeDislikePatch : BytecodePatch(
    listOf(
        TextComponentConstructorFingerprint,
        ShortsTextViewFingerprint,
        DislikesOldLayoutTextViewFingerprint,
        LikeFingerprint,
        DislikeFingerprint,
        RemoveLikeFingerprint,
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        // region Inject newVideoLoaded event handler to update dislikes when a new video is loaded.

        VideoIdPatch.injectCall("$INTEGRATIONS_CLASS_DESCRIPTOR->newVideoLoaded(Ljava/lang/String;)V")

        // endregion

        // region Hook like/dislike/remove like button clicks to send votes to the API.

        listOf(
            LikeFingerprint.toPatch(Vote.LIKE),
            DislikeFingerprint.toPatch(Vote.DISLIKE),
            RemoveLikeFingerprint.toPatch(Vote.REMOVE_LIKE)
        ).forEach { (fingerprint, vote) ->
            fingerprint.result?.mutableMethod?.apply {
                addInstructions(
                    0,
                    """
                        const/4 v0, ${vote.value}
                        invoke-static {v0}, $INTEGRATIONS_CLASS_DESCRIPTOR->sendVote(I)V
                    """
                )
            } ?: return PatchResultError("Failed to find ${fingerprint.name} method.")
        }

        // endregion

        // region Hook creation of Spans and the cached lookup of them.

        // Alternatively the hook can be made at the creation of Spans in TextComponentSpec,
        // And it works in all situations except it fails to update the Span when the user dislikes,
        // since the underlying (likes only) text did not change.
        // This hook handles all situations, as it's where the created Spans are stored and later reused.
        TextComponentContextFingerprint.also {
            it.resolve(
                context,
                TextComponentConstructorFingerprint.result!!.classDef
            )
        }.result?.also { result ->
            if (!TextComponentAtomicReferenceFingerprint.resolve(context, result.method, result.classDef))
                throw TextComponentAtomicReferenceFingerprint.toErrorResult()
        }?.let { textComponentContextFingerprintResult ->
            val conversionContextIndex = textComponentContextFingerprintResult
                .scanResult.patternScanResult!!.startIndex
            val atomicReferenceStartIndex = TextComponentAtomicReferenceFingerprint.result!!
                .scanResult.patternScanResult!!.startIndex

            val insertIndex = atomicReferenceStartIndex + 7

            textComponentContextFingerprintResult.mutableMethod.apply {
                // Get the conversion context obfuscated field name, and the registers for the AtomicReference and CharSequence
                val conversionContextFieldReference =
                    getInstruction<ReferenceInstruction>(conversionContextIndex).reference

                // any free register
                val contextRegister =
                    getInstruction<TwoRegisterInstruction>(atomicReferenceStartIndex).registerB

                val atomicReferenceRegister =
                    getInstruction<FiveRegisterInstruction>(atomicReferenceStartIndex + 4).registerC

                val moveCharSequenceInstruction = getInstruction<TwoRegisterInstruction>(insertIndex)
                val charSequenceRegister = moveCharSequenceInstruction.registerB

                // Insert as first instructions at the control flow label.
                // Must replace the existing instruction to preserve the label, and then insert the remaining instructions.
                replaceInstruction(insertIndex, "move-object/from16 v$contextRegister, p0")
                addInstructions(
                    insertIndex + 1,
                    """
                        iget-object v$contextRegister, v$contextRegister, $conversionContextFieldReference  # copy obfuscated context field into free register
                        invoke-static {v$contextRegister, v$atomicReferenceRegister, v$charSequenceRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->onLithoTextLoaded(Ljava/lang/Object;Ljava/util/concurrent/atomic/AtomicReference;Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                        move-result-object v$charSequenceRegister
                        move-object v${moveCharSequenceInstruction.registerA}, v${charSequenceRegister}  # original instruction at the insertion point
                    """
                )
            }
        } ?: return TextComponentContextFingerprint.toErrorResult()

        // endregion

        // region Hook for Short videos.

        ShortsTextViewFingerprint.result?.let {
            it.mutableMethod.apply {
                val patternResult = it.scanResult.patternScanResult!!

                // If the field is true, the TextView is for a dislike button.
                val isDisLikesBooleanReference = getInstruction<ReferenceInstruction>(patternResult.endIndex).reference

                val textViewFieldReference = // Like/Dislike button TextView field
                    getInstruction<ReferenceInstruction>(patternResult.endIndex - 2).reference

                // Check if the hooked TextView object is that of the dislike button.
                // If RYD is disabled, or the TextView object is not that of the dislike button, the execution flow is not interrupted.
                // Otherwise, the TextView object is modified, and the execution flow is interrupted to prevent it from being changed afterward.
                val insertIndex = patternResult.startIndex + 6
                addInstructionsWithLabels(
                    insertIndex,
                    """
                        # Check, if the TextView is for a dislike button
                        iget-boolean v0, p0, $isDisLikesBooleanReference
                        if-eqz v0, :is_like
                        
                        # Hook the TextView, if it is for the dislike button
                        iget-object v0, p0, $textViewFieldReference
                        invoke-static {v0}, $INTEGRATIONS_CLASS_DESCRIPTOR->setShortsDislikes(Landroid/view/View;)Z
                        move-result v0
                        if-eqz v0, :ryd_disabled
                        return-void
                       
                        :is_like
                        :ryd_disabled
                        nop
                    """
                )
            }
        } ?: return ShortsTextViewFingerprint.toErrorResult()

        // endregion

        // region Hook old UI layout dislikes, for the older app spoofs used with spoof-app-version.

        DislikesOldLayoutTextViewFingerprint.result?.let {
            it.mutableMethod.apply {
                val startIndex = it.scanResult.patternScanResult!!.startIndex

                val resourceIdentifierRegister = getInstruction<OneRegisterInstruction>(startIndex).registerA
                val textViewRegister = getInstruction<OneRegisterInstruction>(startIndex + 4).registerA

                addInstruction(
                    startIndex + 4,
                    "invoke-static {v$resourceIdentifierRegister, v$textViewRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->setOldUILayoutDislikes(ILandroid/widget/TextView;)V"
                )
            }
        } ?: return DislikesOldLayoutTextViewFingerprint.toErrorResult()

        // endregion

        return PatchResultSuccess()
    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR =
            "Lapp/revanced/integrations/patches/ReturnYouTubeDislikePatch;"

        private fun MethodFingerprint.toPatch(voteKind: Vote) = VotePatch(this, voteKind)
        private data class VotePatch(val fingerprint: MethodFingerprint, val voteKind: Vote)
        private enum class Vote(val value: Int) {
            LIKE(1),
            DISLIKE(-1),
            REMOVE_LIKE(0)
        }
    }
}
