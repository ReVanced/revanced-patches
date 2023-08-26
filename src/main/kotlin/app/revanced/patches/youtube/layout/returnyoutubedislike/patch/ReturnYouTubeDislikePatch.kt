package app.revanced.patches.youtube.layout.returnyoutubedislike.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
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
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.returnyoutubedislike.annotations.ReturnYouTubeDislikeCompatibility
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.*
import app.revanced.patches.youtube.layout.returnyoutubedislike.resource.patch.ReturnYouTubeDislikeResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.playertype.patch.PlayerTypeHookPatch
import app.revanced.patches.youtube.video.videoid.patch.VideoIdPatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Patch
@DependsOn(
    [
        IntegrationsPatch::class,
        VideoIdPatch::class,
        ReturnYouTubeDislikeResourcePatch::class,
        PlayerTypeHookPatch::class,
    ]
)
@Name("Return YouTube Dislike")
@Description("Shows the dislike count of videos using the Return YouTube Dislike API.")
@ReturnYouTubeDislikeCompatibility
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
    override fun execute(context: BytecodeContext) {
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
            } ?: throw PatchException("Failed to find ${fingerprint.name} method.")
        }

        // endregion

        // region Hook creation of Spans and the cached lookup of them.

        // Alternatively the hook can be made at the creation of Spans in TextComponentSpec,
        // And it works in all situations except it fails to update the Span when the user dislikes,
        // since the underlying (likes only) text did not change.
        // This hook handles all situations, as it's where the created Spans are stored and later reused.
        TextComponentContextFingerprint.also {
            if (!it.resolve(context, TextComponentConstructorFingerprint.result!!.classDef))
                throw it.exception
        }.result?.also { result ->
            if (!TextComponentAtomicReferenceFingerprint.resolve(context, result.method, result.classDef))
                throw TextComponentAtomicReferenceFingerprint.exception
        }?.let { textComponentContextFingerprintResult ->
            val conversionContextIndex = textComponentContextFingerprintResult
                .scanResult.patternScanResult!!.startIndex
            val atomicReferenceStartIndex = TextComponentAtomicReferenceFingerprint.result!!
                .scanResult.patternScanResult!!.startIndex

            val insertIndex = atomicReferenceStartIndex + 6

            textComponentContextFingerprintResult.mutableMethod.apply {
                // Get the conversion context obfuscated field name, and the registers for the AtomicReference and CharSequence
                val conversionContextFieldReference =
                    getInstruction<ReferenceInstruction>(conversionContextIndex).reference

                // Reuse the free register to make room for the atomic reference register.
                val freeRegister =
                    getInstruction<TwoRegisterInstruction>(atomicReferenceStartIndex).registerB

                val atomicReferenceRegister =
                    getInstruction<FiveRegisterInstruction>(atomicReferenceStartIndex + 1).registerC

                val moveCharSequenceInstruction = getInstruction<TwoRegisterInstruction>(insertIndex - 1)
                val charSequenceSourceRegister = moveCharSequenceInstruction.registerB
                val charSequenceTargetRegister = moveCharSequenceInstruction.registerA

                // In order to preserve the atomic reference register, because it is overwritten,
                // use another free register to store it.
                replaceInstruction(
                    atomicReferenceStartIndex + 2,
                    "move-result-object v$freeRegister"
                )
                replaceInstruction(
                    atomicReferenceStartIndex + 3,
                    "move-object v$charSequenceSourceRegister, v$freeRegister"
                )

                // Move the current instance to the free register, and get the conversion context from it.
                replaceInstruction(insertIndex - 1, "move-object/from16 v$freeRegister, p0")
                addInstructions(
                    insertIndex,
                    """
                        # Move context to free register
                        iget-object v$freeRegister, v$freeRegister, $conversionContextFieldReference 
                        invoke-static {v$freeRegister, v$atomicReferenceRegister, v$charSequenceSourceRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->onLithoTextLoaded(Ljava/lang/Object;Ljava/util/concurrent/atomic/AtomicReference;Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                        move-result-object v$freeRegister
                        # Replace the original char sequence with the modified one.
                        move-object v${charSequenceTargetRegister}, v${freeRegister}
                    """
                )
            }
        } ?: throw TextComponentContextFingerprint.exception

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
        } ?: throw ShortsTextViewFingerprint.exception

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
        } ?: throw DislikesOldLayoutTextViewFingerprint.exception

        // endregion
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
