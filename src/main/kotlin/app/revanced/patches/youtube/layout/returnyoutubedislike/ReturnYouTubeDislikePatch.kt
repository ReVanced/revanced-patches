package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.ConversionContextFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.DislikeFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.DislikesOldLayoutTextViewFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.LikeFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.RemoveLikeFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.RollingNumberMeasureAnimatedTextFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.RollingNumberMeasureStaticLabelFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.RollingNumberMeasureStaticLabelParentFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.RollingNumberSetterFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.RollingNumberTextViewFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.ShortsTextViewFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.TextComponentConstructorFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.TextComponentDataFingerprint
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.TextComponentLookupFingerprint
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.LithoFilterPatch
import app.revanced.patches.youtube.misc.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.shared.fingerprints.RollingNumberTextViewAnimationUpdateFingerprint
import app.revanced.patches.youtube.video.videoid.VideoIdPatch
import app.revanced.util.exception
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

@Patch(
    name = "Return YouTube Dislike",
    description = "Adds an option to show the dislike count of videos using the Return YouTube Dislike API.",
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
object ReturnYouTubeDislikePatch : BytecodePatch(
    setOf(
        ConversionContextFingerprint,
        TextComponentConstructorFingerprint,
        TextComponentDataFingerprint,
        ShortsTextViewFingerprint,
        DislikesOldLayoutTextViewFingerprint,
        LikeFingerprint,
        DislikeFingerprint,
        RemoveLikeFingerprint,
        RollingNumberSetterFingerprint,
        RollingNumberMeasureStaticLabelParentFingerprint,
        RollingNumberMeasureAnimatedTextFingerprint,
        RollingNumberTextViewFingerprint,
        RollingNumberTextViewAnimationUpdateFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/ReturnYouTubeDislikePatch;"

    private const val FILTER_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/components/ReturnYouTubeDislikeFilterPatch;"

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

        // region Hook code for creation and cached lookup of text Spans.

        // Alternatively the hook can be made at the creation of Spans in TextComponentSpec,
        // And it works in all situations except it fails to update the Span when the user dislikes,
        // since the underlying (likes only) text did not change.
        // This hook handles all situations, as it's where the created Spans are stored and later reused.
        TextComponentConstructorFingerprint.result?.let { textConstructorResult ->
            // Find the field name of the conversion context.
            val conversionContextClassType = ConversionContextFingerprint.resultOrThrow().classDef.type
            val conversionContextField = textConstructorResult.classDef.fields.find {
                it.type == conversionContextClassType
            } ?: throw PatchException("Could not find conversion context field")

            TextComponentLookupFingerprint.resolve(context, textConstructorResult.classDef)
            TextComponentLookupFingerprint.resultOrThrow().mutableMethod.apply {
                // Find the instruction for creating the text data object.
                val textDataClassType = TextComponentDataFingerprint.resultOrThrow().classDef.type
                val insertIndex = indexOfFirstInstruction {
                    opcode == Opcode.NEW_INSTANCE &&
                            getReference<TypeReference>()?.type == textDataClassType
                }
                if (insertIndex < 0) throw PatchException("Could not find data creation instruction")
                val tempRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                // Find the instruction that sets the span to an instance field.
                // The instruction is only a few lines after the creation of the instance.
                // The method has multiple iput-object instructions using a CharSequence,
                // so verify the found instruction is in the expected location.
                val putFieldInstruction = implementation!!.instructions
                    .subList(insertIndex, insertIndex + 20)
                    .find {
                        it.opcode == Opcode.IPUT_OBJECT &&
                                it.getReference<FieldReference>()?.type == "Ljava/lang/CharSequence;"
                    } ?: throw PatchException("Could not find put object instruction")
                val charSequenceRegister = (putFieldInstruction as TwoRegisterInstruction).registerA

                addInstructions(
                    insertIndex,
                    """
                    # Copy conversion context
                    move-object/from16 v$tempRegister, p0
                    iget-object v$tempRegister, v$tempRegister, $conversionContextField
                    invoke-static {v$tempRegister, v$charSequenceRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->onLithoTextLoaded(Ljava/lang/Object;Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                    move-result-object v$charSequenceRegister
                """
                )
            }
        } ?: throw TextComponentConstructorFingerprint.exception

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


        // region Hook rolling numbers.

        // Do this last to allow patching old unsupported versions (if the user really wants),
        // On older unsupported version this will fail to resolve and throw an exception,
        // but everything will still work correctly anyways.

        RollingNumberSetterFingerprint.result?.let {
            val dislikesIndex = it.scanResult.patternScanResult!!.endIndex

            it.mutableMethod.apply {
                val insertIndex = 1

                val charSequenceInstanceRegister =
                    getInstruction<OneRegisterInstruction>(0).registerA
                val charSequenceFieldReference =
                    getInstruction<ReferenceInstruction>(dislikesIndex).reference

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
        RollingNumberMeasureAnimatedTextFingerprint.result?.also {
            val scanResult = it.scanResult.patternScanResult!!
            // Additional check to verify the opcodes are at the start of the method
            if (scanResult.startIndex != 0) throw PatchException("Unexpected opcode location")
            val endIndex = scanResult.endIndex
            it.mutableMethod.apply {
                val measuredTextWidthRegister = getInstruction<OneRegisterInstruction>(endIndex).registerA

                addInstructions(
                    endIndex + 1,
                    """
                    invoke-static {p1, v$measuredTextWidthRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->onRollingNumberMeasured(Ljava/lang/String;F)F
                    move-result v$measuredTextWidthRegister
                """
                )
            }
        } ?: throw RollingNumberMeasureAnimatedTextFingerprint.exception

        // Additional text measurement method. Used if YouTube decides not to animate the likes count
        // and sometimes used for initial video load.
        RollingNumberMeasureStaticLabelFingerprint.resolve(context, RollingNumberMeasureStaticLabelParentFingerprint.resultOrThrow().classDef)
        RollingNumberMeasureStaticLabelFingerprint.result?.also {
            val measureTextIndex = it.scanResult.patternScanResult!!.startIndex + 1
            it.mutableMethod.apply {
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

    }

    private fun MethodFingerprint.toPatch(voteKind: Vote) = VotePatch(this, voteKind)
    private data class VotePatch(val fingerprint: MethodFingerprint, val voteKind: Vote)
    private enum class Vote(val value: Int) {
        LIKE(1),
        DISLIKE(-1),
        REMOVE_LIKE(0)
    }
}
