package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.IntentPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.playservice.is_19_33_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.addSettingPreference
import app.revanced.patches.youtube.misc.settings.newIntent
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.rollingNumberTextViewAnimationUpdateFingerprint
import app.revanced.patches.youtube.video.videoid.hookPlayerResponseVideoId
import app.revanced.patches.youtube.video.videoid.hookVideoId
import app.revanced.patches.youtube.video.videoid.videoIdPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.sun.org.apache.bcel.internal.generic.InstructionConst.getInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/ReturnYouTubeDislikePatch;"

private const val FILTER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/components/ReturnYouTubeDislikeFilterPatch;"

@Suppress("unused")
val returnYouTubeDislikePatch = bytecodePatch(
    name = "Return YouTube Dislike",
    description = "Adds an option to show the dislike count of videos with Return YouTube Dislike.",
) {
    dependsOn(
        settingsPatch,
        sharedExtensionPatch,
        addResourcesPatch,
        lithoFilterPatch,
        videoIdPatch,
        playerTypeHookPatch,
        versionCheckPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.49.37",
            "19.16.39",
            "19.25.37",
            "19.34.42",
            "19.43.41",
        ),
    )

    execute {
        addResources("youtube", "layout.returnyoutubedislike.returnYouTubeDislikePatch")

        addSettingPreference(
            IntentPreference(
                key = "revanced_settings_screen_09",
                titleKey = "revanced_ryd_settings_title",
                summaryKey = null,
                intent = newIntent("revanced_ryd_settings_intent"),
            ),
        )

        // region Inject newVideoLoaded event handler to update dislikes when a new video is loaded.

        hookVideoId("$EXTENSION_CLASS_DESCRIPTOR->newVideoLoaded(Ljava/lang/String;)V")

        // Hook the player response video id, to start loading RYD sooner in the background.
        hookPlayerResponseVideoId("$EXTENSION_CLASS_DESCRIPTOR->preloadVideoId(Ljava/lang/String;Z)V")

        // endregion

        // region Hook like/dislike/remove like button clicks to send votes to the API.

        mapOf(
            likeFingerprint to Vote.LIKE,
            dislikeFingerprint to Vote.DISLIKE,
            removeLikeFingerprint to Vote.REMOVE_LIKE,
        ).forEach { (fingerprint, vote) ->
            val match by fingerprint
            match.method.addInstructions(
                0,
                """
                    const/4 v0, ${vote.value}
                    invoke-static {v0}, $EXTENSION_CLASS_DESCRIPTOR->sendVote(I)V
                """,
            )
        }

        // endregion

        // region Hook code for creation and cached lookup of text Spans.

        // Alternatively the hook can be made at tht it fails to update the Span when the user dislikes,
        //        // since the underlying (likes only) tee creation of Spans in TextComponentSpec,
        // And it works in all situations excepxt did not change.
        // This hook handles all situations, as it's where the created Spans are stored and later reused.
        // Find the field name of the conversion context.
        val textComponentConstructorMatch by textComponentConstructorFingerprint
        val conversionContextField = textComponentConstructorMatch.originalClassDef.fields.find {
            it.type == conversionContextFingerprint.matchOrThrow.originalClassDef.type
        } ?: throw PatchException("Could not find conversion context field")

        textComponentLookupFingerprint.matchOrThrow(textComponentConstructorFingerprint.matchOrThrow.originalClassDef)
        textComponentLookupFingerprint.matchOrThrow.method.apply {
            // Find the instruction for creating the text data object.
            val textDataClassType = textComponentDataFingerprint.matchOrThrow.originalClassDef.type

            val insertIndex: Int
            val tempRegister: Int
            val charSequenceRegister: Int

            if (is_19_33_or_greater) {
                insertIndex = indexOfFirstInstructionOrThrow {
                    opcode == Opcode.INVOKE_STATIC_RANGE &&
                        getReference<MethodReference>()?.returnType == textDataClassType
                }

                tempRegister = getInstruction<OneRegisterInstruction>(insertIndex + 1).registerA

                // Find the instruction that sets the span to an instance field.
                // The instruction is only a few lines after the creation of the instance.
                charSequenceRegister = getInstruction<FiveRegisterInstruction>(
                    indexOfFirstInstructionOrThrow(insertIndex) {
                        opcode == Opcode.INVOKE_VIRTUAL &&
                            getReference<MethodReference>()?.parameterTypes?.firstOrNull() == "Ljava/lang/CharSequence;"
                    },
                ).registerD
            } else {
                insertIndex = indexOfFirstInstructionOrThrow {
                    opcode == Opcode.NEW_INSTANCE &&
                        getReference<TypeReference>()?.type == textDataClassType
                }

                tempRegister = getInstruction<OneRegisterInstruction>(insertIndex).registerA

                charSequenceRegister = getInstruction<TwoRegisterInstruction>(
                    indexOfFirstInstructionOrThrow(insertIndex) {
                        opcode == Opcode.IPUT_OBJECT &&
                            getReference<FieldReference>()?.type == "Ljava/lang/CharSequence;"
                    },
                ).registerA
            }

            addInstructionsAtControlFlowLabel(
                insertIndex,
                """
                        # Copy conversion context
                        move-object/from16 v$tempRegister, p0
                        iget-object v$tempRegister, v$tempRegister, $conversionContextField
                        invoke-static { v$tempRegister, v$charSequenceRegister }, $EXTENSION_CLASS_DESCRIPTOR->onLithoTextLoaded(Ljava/lang/Object;Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                        move-result-object v$charSequenceRegister
                    """,
            )
        }

        // endregion

        // region Hook for non-litho Short videos.
        val shortsTextViewMatch by shortsTextViewFingerprint
        shortsTextViewMatch.method.apply {
            val insertIndex = shortsTextViewMatch.patternMatch!!.endIndex + 1

            // If the field is true, the TextView is for a dislike button.
            val isDisLikesBooleanInstruction = instructions.first { instruction ->
                instruction.opcode == Opcode.IGET_BOOLEAN
            } as ReferenceInstruction

            val isDisLikesBooleanReference = isDisLikesBooleanInstruction.reference

            // Like/Dislike button TextView field.
            val textViewFieldInstruction = instructions.first { instruction ->
                instruction.opcode == Opcode.IGET_OBJECT
            } as ReferenceInstruction

            val textViewFieldReference = textViewFieldInstruction.reference

            // Check if the hooked TextView object is that of the dislike button.
            // If RYD is disabled, or the TextView object is not that of the dislike button, the execution flow is not interrupted.
            // Otherwise, the TextView object is modified, and the execution flow is interrupted to prevent it from being changed afterward.
            addInstructionsWithLabels(
                insertIndex,
                """
                        # Check, if the TextView is for a dislike button
                        iget-boolean v0, p0, $isDisLikesBooleanReference
                        if-eqz v0, :is_like
                        
                        # Hook the TextView, if it is for the dislike button
                        iget-object v0, p0, $textViewFieldReference
                        invoke-static {v0}, $EXTENSION_CLASS_DESCRIPTOR->setShortsDislikes(Landroid/view/View;)Z
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

        // region Hook rolling numbers.

        // Do this last to allow patching old unsupported versions (if the user really wants),
        // On older unsupported version this will fail to match and throw an exception,
        // but everything will still work correctly anyway.
        val rollingNumberSetterMatch by rollingNumberSetterFingerprint
        val dislikesIndex = rollingNumberSetterMatch.patternMatch!!.endIndex

        rollingNumberSetterMatch.method.apply {
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
                    invoke-static {v$conversionContextRegister, v$freeRegister}, $EXTENSION_CLASS_DESCRIPTOR->onRollingNumberLoaded(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$freeRegister
                    iput-object v$freeRegister, v$charSequenceInstanceRegister, $charSequenceFieldReference
                """,
            )
        }

        // Rolling Number text views use the measured width of the raw string for layout.
        // Modify the measure text calculation to include the left drawable separator if needed.
        val rollingNumberMeasureAnimatedTextMatch by rollingNumberMeasureAnimatedTextFingerprint
        val patternMatch = rollingNumberMeasureAnimatedTextMatch.patternMatch!!
        // Additional check to verify the opcodes are at the start of the method
        if (patternMatch.startIndex != 0) throw PatchException("Unexpected opcode location")
        val endIndex = patternMatch.endIndex
        rollingNumberMeasureAnimatedTextMatch.method.apply {
            val measuredTextWidthRegister = getInstruction<OneRegisterInstruction>(endIndex).registerA

            addInstructions(
                endIndex + 1,
                """
                    invoke-static {p1, v$measuredTextWidthRegister}, $EXTENSION_CLASS_DESCRIPTOR->onRollingNumberMeasured(Ljava/lang/String;F)F
                    move-result v$measuredTextWidthRegister
                """,
            )
        }

        // Additional text measurement method. Used if YouTube decides not to animate the likes count
        // and sometimes used for initial video load.
        rollingNumberMeasureStaticLabelFingerprint.matchOrThrow(
            rollingNumberMeasureStaticLabelParentFingerprint.matchOrThrow.originalClassDef,
        ).let {
            val measureTextIndex = it.patternMatch!!.startIndex + 1
            it.method.apply {
                val freeRegister = getInstruction<TwoRegisterInstruction>(0).registerA

                addInstructions(
                    measureTextIndex + 1,
                    """
                        move-result v$freeRegister
                        invoke-static {p1, v$freeRegister}, $EXTENSION_CLASS_DESCRIPTOR->onRollingNumberMeasured(Ljava/lang/String;F)F
                    """,
                )
            }
        }
        // The rolling number Span is missing styling since it's initially set as a String.
        // Modify the UI text view and use the styled like/dislike Span.
        // Initial TextView is set in this method.
        val initiallyCreatedTextViewMethod = rollingNumberTextViewFingerprint.matchOrThrow.method

        // Videos less than 24 hours after uploaded, like counts will be updated in real time.
        // Whenever like counts are updated, TextView is set in this method.
        arrayOf(
            initiallyCreatedTextViewMethod,
            rollingNumberTextViewAnimationUpdateFingerprint.matchOrThrow.method,
        ).forEach { insertMethod ->
            insertMethod.apply {
                val setTextIndex = indexOfFirstInstructionOrThrow {
                    getReference<MethodReference>()?.name == "setText"
                }

                val textViewRegister =
                    getInstruction<FiveRegisterInstruction>(setTextIndex).registerC
                val textSpanRegister =
                    getInstruction<FiveRegisterInstruction>(setTextIndex).registerD

                addInstructions(
                    setTextIndex,
                    """
                            invoke-static {v$textViewRegister, v$textSpanRegister}, $EXTENSION_CLASS_DESCRIPTOR->updateRollingNumber(Landroid/widget/TextView;Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
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
