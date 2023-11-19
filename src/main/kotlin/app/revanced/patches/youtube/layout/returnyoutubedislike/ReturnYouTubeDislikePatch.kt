package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.extensions.exception
import app.revanced.extensions.getReference
import app.revanced.extensions.indexOfFirstInstruction
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.*
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.shared.fingerprints.RollingNumberTextViewAnimationUpdateFingerprint
import app.revanced.patches.youtube.video.videoid.VideoIdPatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Return YouTube Dislike",
    description = "Shows the dislike count of videos using the Return YouTube Dislike API.",
    dependencies = [
        IntegrationsPatch::class,
        LithoFilterPatch::class,
        VideoIdPatch::class,
        ReturnYouTubeDislikeResourcePatch::class,
        PlayerTypeHookPatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube", [
                "18.43.45",
                "18.44.41",
                "18.45.41"
            ]
        )
    ]
)
@Suppress("unused")
object ReturnYouTubeDislikePatch : BytecodePatch(
    setOf(
        TextComponentConstructorFingerprint,
        ShortsTextViewFingerprint,
        DislikesOldLayoutTextViewFingerprint,
        LikeFingerprint,
        DislikeFingerprint,
        RemoveLikeFingerprint,
        RollingNumberSetterFingerprint,
        RollingNumberMeasureTextParentFingerprint,
        RollingNumberTextViewFingerprint,
        RollingNumberTextViewAnimationUpdateFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/ReturnYouTubeDislikePatch;"

    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/components/ReturnYouTubeDislikeFilterPatch;"

    override fun execute(context: BytecodeContext) {
        // region Inject newVideoLoaded event handler to update dislikes when a new video is loaded.

        VideoIdPatch.hookVideoId("$INTEGRATIONS_CLASS_DESCRIPTOR->newVideoLoaded(Ljava/lang/String;)V")

        // Hook the player response video id, to start loading RYD sooner in the background.
        VideoIdPatch.hookPlayerResponseVideoId("$INTEGRATIONS_CLASS_DESCRIPTOR->preloadVideoId(Ljava/lang/String;Z)V")

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
            } ?: throw fingerprint.exception
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
                .scanResult.patternScanResult!!.endIndex
            val atomicReferenceStartIndex = TextComponentAtomicReferenceFingerprint.result!!
                .scanResult.patternScanResult!!.startIndex

            val insertIndex = atomicReferenceStartIndex + 9

            textComponentContextFingerprintResult.mutableMethod.apply {
                // Get the conversion context obfuscated field name
                val conversionContextFieldReference =
                    getInstruction<ReferenceInstruction>(conversionContextIndex).reference

                // Free register to hold the conversion context
                val freeRegister =
                    getInstruction<TwoRegisterInstruction>(atomicReferenceStartIndex).registerB

                val atomicReferenceRegister =
                    getInstruction<FiveRegisterInstruction>(atomicReferenceStartIndex + 6).registerC

                // Instruction that is replaced, and also has the CharacterSequence register.
                val moveCharSequenceInstruction = getInstruction<TwoRegisterInstruction>(insertIndex)
                val charSequenceSourceRegister = moveCharSequenceInstruction.registerB
                val charSequenceTargetRegister = moveCharSequenceInstruction.registerA

                // Move the current instance to the free register, and get the conversion context from it.
                // Must replace the instruction to preserve the control flow label.
                replaceInstruction(insertIndex, "move-object/from16 v$freeRegister, p0")
                addInstructions(
                    insertIndex + 1,
                    """
                        # Move context to free register
                        iget-object v$freeRegister, v$freeRegister, $conversionContextFieldReference
                        invoke-static {v$freeRegister, v$atomicReferenceRegister, v$charSequenceSourceRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->onLithoTextLoaded(Ljava/lang/Object;Ljava/util/concurrent/atomic/AtomicReference;Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                        move-result-object v$freeRegister
                        # Replace the original instruction
                        move-object v${charSequenceTargetRegister}, v${freeRegister}
                    """
                )
            }
        } ?: throw TextComponentContextFingerprint.exception

        // endregion

        // region Hook rolling numbers.

        RollingNumberSetterFingerprint.result?.let {
            val dislikesIndex = it.scanResult.patternScanResult!!.endIndex

            it.mutableMethod.apply {
                val insertIndex = 1

                val charSequenceInstanceRegister =
                    getInstruction<OneRegisterInstruction>(0).registerA
                val charSequenceFieldReference =
                    getInstruction<ReferenceInstruction>(dislikesIndex).reference.toString()

                val registerCount = implementation!!.registerCount

                // This register is being overwritten, so it is free to use.
                val freeRegister = registerCount - 1
                val conversionContextRegister = registerCount - parameters.size + 1

                addInstructions(
                    insertIndex,
                    """
                        iget-object v$freeRegister, v$charSequenceInstanceRegister, $charSequenceFieldReference
                        invoke-static {v$conversionContextRegister, v$freeRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->onRollingNumberLoaded(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$freeRegister
                        iput-object v$freeRegister, v$charSequenceInstanceRegister, $charSequenceFieldReference
                    """
                )
            }
        } ?: throw RollingNumberSetterFingerprint.exception

        // Rolling Number text views use the measured width of the raw string for layout.
        // Modify the measure text calculation to include the left drawable separator if needed.
        RollingNumberMeasureAnimatedTextFingerprint.also {
            if (!it.resolve(context, RollingNumberMeasureTextParentFingerprint.result!!.classDef))
                throw it.exception
        }.result?.also {
            it.mutableMethod.apply {
                val returnInstructionIndex = it.scanResult.patternScanResult!!.endIndex
                val measuredTextWidthRegister =
                    getInstruction<OneRegisterInstruction>(returnInstructionIndex).registerA

                replaceInstruction( // Replace instruction to preserve control flow label.
                    returnInstructionIndex,
                    "invoke-static {p1, v$measuredTextWidthRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->onRollingNumberMeasured(Ljava/lang/String;F)F"
                )
                addInstructions(
                    returnInstructionIndex + 1,
                    """
                        move-result v$measuredTextWidthRegister
                        return v$measuredTextWidthRegister
                    """
                )
            }
        } ?: throw RollingNumberMeasureAnimatedTextFingerprint.exception

        // Additional text measurement method. Used if YouTube decides not to animate the likes count
        // and sometimes used for initial video load.
        RollingNumberMeasureStaticLabelFingerprint.also {
            if (!it.resolve(context, RollingNumberMeasureTextParentFingerprint.result!!.classDef))
                throw it.exception
        }.result?.also {
            it.mutableMethod.apply {
                val measureTextIndex = it.scanResult.patternScanResult!!.startIndex + 1
                val freeRegister = getInstruction<TwoRegisterInstruction>(0).registerA

                addInstructions(
                    measureTextIndex + 1,
                    """
                        move-result v$freeRegister
                        invoke-static {p1, v$freeRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->onRollingNumberMeasured(Ljava/lang/String;F)F
                    """
                )
            }
        } ?: throw RollingNumberMeasureStaticLabelFingerprint.exception

        // The rolling number Span is missing styling since it's initially set as a String.
        // Modify the UI text view and use the styled like/dislike Span.
        RollingNumberTextViewFingerprint.result?.let {
            // Initial TextView is set in this method.
            val initiallyCreatedTextViewMethod = it.mutableMethod

            // Videos less than 24 hours after uploaded, like counts will be updated in real time.
            // Whenever like counts are updated, TextView is set in this method.
            val realTimeUpdateTextViewMethod =
                RollingNumberTextViewAnimationUpdateFingerprint.result?.mutableMethod
                    ?: throw RollingNumberTextViewAnimationUpdateFingerprint.exception

            arrayOf(
                initiallyCreatedTextViewMethod,
                realTimeUpdateTextViewMethod
            ).forEach { insertMethod ->
                insertMethod.apply {
                    val setTextIndex = indexOfFirstInstruction {
                        getReference<MethodReference>()?.name == "setText"
                    }

                    val textViewRegister =
                        getInstruction<FiveRegisterInstruction>(setTextIndex).registerC
                    val textSpanRegister =
                        getInstruction<FiveRegisterInstruction>(setTextIndex).registerD

                    addInstructions(
                        setTextIndex,
                        """
                            invoke-static {v$textViewRegister, v$textSpanRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->updateRollingNumber(Landroid/widget/TextView;Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                            move-result-object v$textSpanRegister
                        """
                    )
                }
            }
        } ?: throw RollingNumberTextViewFingerprint.exception

        // endregion

        // region Hook for non-litho Short videos.

        ShortsTextViewFingerprint.result?.let {
            it.mutableMethod.apply {
                val patternResult = it.scanResult.patternScanResult!!

                // If the field is true, the TextView is for a dislike button.
                val isDisLikesBooleanReference = getInstruction<ReferenceInstruction>(patternResult.endIndex).reference

                val textViewFieldReference = // Like/Dislike button TextView field
                    getInstruction<ReferenceInstruction>(patternResult.endIndex - 1).reference

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

        // region Hook for litho Shorts

        // Filter that parses the video id from the UI
        LithoFilterPatch.addFilter(FILTER_CLASS_DESCRIPTOR)

        // Player response video id is needed to search for the video ids in Shorts litho components.
        VideoIdPatch.hookPlayerResponseVideoId("$FILTER_CLASS_DESCRIPTOR->newPlayerResponseVideoId(Ljava/lang/String;Z)V")

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

    private fun MethodFingerprint.toPatch(voteKind: Vote) = VotePatch(this, voteKind)
    private data class VotePatch(val fingerprint: MethodFingerprint, val voteKind: Vote)
    private enum class Vote(val value: Int) {
        LIKE(1),
        DISLIKE(-1),
        REMOVE_LIKE(0)
    }
}
