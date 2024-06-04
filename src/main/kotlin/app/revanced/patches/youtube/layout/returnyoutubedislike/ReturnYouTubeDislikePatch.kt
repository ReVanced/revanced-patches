package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.MethodFingerprintResult
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints.*
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.shared.fingerprints.rollingNumberTextViewAnimationUpdateFingerprint
import app.revanced.patches.youtube.video.videoid.hookPlayerResponseVideoId
import app.revanced.patches.youtube.video.videoid.hookVideoId
import app.revanced.patches.youtube.video.videoid.videoIdPatch
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

private const val INTEGRATIONS_CLASS_DESCRIPTOR =
    "Lapp/revanced/integrations/youtube/patches/ReturnYouTubeDislikePatch;"

private const val FILTER_CLASS_DESCRIPTOR =
    "Lapp/revanced/integrations/youtube/patches/components/ReturnYouTubeDislikeFilterPatch;"

@Suppress("unused")
val returnYouTubeDislikePatch = bytecodePatch(
    name = "Return YouTube Dislike",
    description = "Adds an option to show the dislike count of videos with Return YouTube Dislike.",
) {
    dependsOn(
        integrationsPatch,
        lithoFilterPatch,
        videoIdPatch,
        returnYouTubeDislikeResourcePatch,
        playerTypeHookPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
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
            "19.11.43",
        ),
    )

    val conversionContextResult by conversionContextFingerprint
    val textComponentConstructorResult by textComponentConstructorFingerprint
    val textComponentDataResult by textComponentDataFingerprint
    val shortsTextViewResult by shortsTextViewFingerprint
    val dislikesOldLayoutTextViewResult by dislikesOldLayoutTextViewFingerprint
    val likeResult by likeFingerprint
    val dislikeResult by dislikeFingerprint
    val removeLikeResult by removeLikeFingerprint
    val rollingNumberSetterResult by rollingNumberSetterFingerprint
    val rollingNumberMeasureStaticLabelParentResult by rollingNumberMeasureStaticLabelParentFingerprint
    val rollingNumberMeasureAnimatedTextResult by rollingNumberMeasureAnimatedTextFingerprint
    val rollingNumberTextViewResult by rollingNumberTextViewFingerprint
    val rollingNumberTextViewAnimationUpdateResult by rollingNumberTextViewAnimationUpdateFingerprint

    execute { context -> // region Inject newVideoLoaded event handler to update dislikes when a new video is loaded.

        hookVideoId("$INTEGRATIONS_CLASS_DESCRIPTOR->newVideoLoaded(Ljava/lang/String;)V")

        // Hook the player response video id, to start loading RYD sooner in the background.
        hookPlayerResponseVideoId("$INTEGRATIONS_CLASS_DESCRIPTOR->preloadVideoId(Ljava/lang/String;Z)V")

        // endregion

        // region Hook like/dislike/remove like button clicks to send votes to the API.

        data class VotePatch(val fingerprint: MethodFingerprintResult, val voteKind: Vote)

        listOf(
            VotePatch(likeResult, Vote.LIKE),
            VotePatch(dislikeResult, Vote.DISLIKE),
            VotePatch(removeLikeResult, Vote.REMOVE_LIKE),
        ).forEach { (result, vote) ->
            result.mutableMethod.addInstructions(
                0,
                """
                    const/4 v0, ${vote.value}
                    invoke-static {v0}, $INTEGRATIONS_CLASS_DESCRIPTOR->sendVote(I)V
                """,
            )
        }

        // endregion

        // region Hook code for creation and cached lookup of text Spans.

        // Alternatively the hook can be made at the creation of Spans in TextComponentSpec,
        // And it works in all situations except it fails to update the Span when the user dislikes,
        // since the underlying (likes only) text did not change.
        // This hook handles all situations, as it's where the created Spans are stored and later reused.
        // Find the field name of the conversion context.
        val conversionContextField = textComponentConstructorResult.classDef.fields.find {
            it.type == conversionContextResult.classDef.type
        } ?: throw PatchException("Could not find conversion context field")

        textComponentLookupFingerprint.apply {
            resolve(context, textComponentConstructorResult.classDef)
        }.resultOrThrow().mutableMethod.apply {
            // Find the instruction for creating the text data object.
            val insertIndex = indexOfFirstInstruction {
                opcode == Opcode.NEW_INSTANCE &&
                    getReference<TypeReference>()?.type == textComponentDataResult.classDef.type
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
                """,
            )
        }

        // endregion

        // region Hook for non-litho Short videos.

        shortsTextViewResult.mutableMethod.apply {
            val patternResult = shortsTextViewResult.scanResult.patternScanResult!!

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
                """,
            )
        }

        // endregion

        // region Hook for litho Shorts

        // Filter that parses the video id from the UI
        addLithoFilter(FILTER_CLASS_DESCRIPTOR)

        // Player response video id is needed to search for the video ids in Shorts litho components.
        hookPlayerResponseVideoId("$FILTER_CLASS_DESCRIPTOR->newPlayerResponseVideoId(Ljava/lang/String;Z)V")

        // endregion

        // region Hook old UI layout dislikes, for the older app spoofs used with spoof-app-version.

        dislikesOldLayoutTextViewResult.mutableMethod.apply {
            val startIndex = dislikesOldLayoutTextViewResult.scanResult.patternScanResult!!.startIndex

            val resourceIdentifierRegister = getInstruction<OneRegisterInstruction>(startIndex).registerA
            val textViewRegister = getInstruction<OneRegisterInstruction>(startIndex + 4).registerA

            addInstruction(
                startIndex + 4,
                "invoke-static {v$resourceIdentifierRegister, v$textViewRegister}, " +
                    "$INTEGRATIONS_CLASS_DESCRIPTOR->setOldUILayoutDislikes(ILandroid/widget/TextView;)V",
            )
        }

        // endregion

        // region Hook rolling numbers.

        // Do this last to allow patching old unsupported versions (if the user really wants),
        // On older unsupported version this will fail to resolve and throw an exception,
        // but everything will still work correctly anyways.

        val dislikesIndex = rollingNumberSetterResult.scanResult.patternScanResult!!.endIndex

        rollingNumberSetterResult.mutableMethod.apply {
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
                """,
            )
        }

        // Rolling Number text views use the measured width of the raw string for layout.
        // Modify the measure text calculation to include the left drawable separator if needed.
        val scanResult = rollingNumberMeasureAnimatedTextResult.scanResult.patternScanResult!!
        // Additional check to verify the opcodes are at the start of the method
        if (scanResult.startIndex != 0) throw PatchException("Unexpected opcode location")
        val endIndex = scanResult.endIndex
        rollingNumberMeasureAnimatedTextResult.mutableMethod.apply {
            val measuredTextWidthRegister = getInstruction<OneRegisterInstruction>(endIndex).registerA

            addInstructions(
                endIndex + 1,
                """
                    invoke-static {p1, v$measuredTextWidthRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->onRollingNumberMeasured(Ljava/lang/String;F)F
                    move-result v$measuredTextWidthRegister
                """,
            )
        }

        // Additional text measurement method. Used if YouTube decides not to animate the likes count
        // and sometimes used for initial video load.
        rollingNumberMeasureStaticLabelFingerprint.apply {
            resolve(
                context,
                rollingNumberMeasureStaticLabelParentResult.classDef,
            )
        }.result?.let {
            val measureTextIndex = it.scanResult.patternScanResult!!.startIndex + 1
            it.mutableMethod.apply {
                val freeRegister = getInstruction<TwoRegisterInstruction>(0).registerA

                addInstructions(
                    measureTextIndex + 1,
                    """
                        move-result v$freeRegister
                        invoke-static {p1, v$freeRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->onRollingNumberMeasured(Ljava/lang/String;F)F
                    """,
                )
            }
        } ?: throw rollingNumberMeasureStaticLabelFingerprint.exception

        // The rolling number Span is missing styling since it's initially set as a String.
        // Modify the UI text view and use the styled like/dislike Span.
        // Initial TextView is set in this method.
        val initiallyCreatedTextViewMethod = rollingNumberTextViewResult.mutableMethod

        // Videos less than 24 hours after uploaded, like counts will be updated in real time.
        // Whenever like counts are updated, TextView is set in this method.
        arrayOf(
            initiallyCreatedTextViewMethod,
            rollingNumberTextViewAnimationUpdateResult.mutableMethod,
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
                        """,
                )
            }
        }

        // endregion
    }
}

enum class Vote(val value: Int) {
    LIKE(1),
    DISLIKE(-1),
    REMOVE_LIKE(0),
}
