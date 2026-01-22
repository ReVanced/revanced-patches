package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.NonInteractivePreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceCategory
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.playservice.is_19_33_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_07_or_greater
import app.revanced.patches.youtube.misc.playservice.is_20_10_or_greater
import app.revanced.patches.youtube.misc.playservice.versionCheckPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.conversionContextFingerprintToString
import app.revanced.patches.youtube.shared.rollingNumberTextViewAnimationUpdateFingerprint
import app.revanced.patches.youtube.video.videoid.hookPlayerResponseVideoId
import app.revanced.patches.youtube.video.videoid.hookVideoId
import app.revanced.patches.youtube.video.videoid.videoIdPatch
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.returnLate
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/ReturnYouTubeDislikePatch;"

private const val FILTER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/components/ReturnYouTubeDislikeFilter;"

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
            "19.34.42",
            "20.07.39",
            "20.13.41",
            "20.14.43",
        )
    )

    execute {
        addResources("youtube", "layout.returnyoutubedislike.returnYouTubeDislikePatch")

        PreferenceScreen.RETURN_YOUTUBE_DISLIKE.addPreferences(
            SwitchPreference("revanced_ryd_enabled"),
            SwitchPreference("revanced_ryd_shorts"),
            SwitchPreference("revanced_ryd_dislike_percentage"),
            SwitchPreference("revanced_ryd_compact_layout"),
            SwitchPreference("revanced_ryd_estimated_like"),
            SwitchPreference("revanced_ryd_toast_on_connection_error"),
            NonInteractivePreference(
                key = "revanced_ryd_attribution",
                tag = "app.revanced.extension.youtube.returnyoutubedislike.ui.ReturnYouTubeDislikeAboutPreference",
                selectable = true,
            ),
            PreferenceCategory(
                key = "revanced_ryd_statistics_category",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = emptySet(), // Preferences are added by custom class at runtime.
                tag = "app.revanced.extension.youtube.returnyoutubedislike.ui.ReturnYouTubeDislikeDebugStatsPreferenceCategory"
            )
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
            fingerprint.method.addInstructions(
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
        val conversionContextField = textComponentConstructorFingerprint.originalClassDef.fields.find {
            it.type == conversionContextFingerprintToString.originalClassDef.type
        } ?: throw PatchException("Could not find conversion context field")

        textComponentLookupFingerprint.match(textComponentConstructorFingerprint.originalClassDef)
            .method.apply {
            // Find the instruction for creating the text data object.
            val textDataClassType = textComponentDataFingerprint.originalClassDef.type

            val insertIndex: Int
            val tempRegister: Int
            val charSequenceRegister: Int

            if (is_19_33_or_greater  && !is_20_10_or_greater) {
                insertIndex = indexOfFirstInstructionOrThrow {
                    (opcode == Opcode.INVOKE_STATIC || opcode == Opcode.INVOKE_STATIC_RANGE)
                            && getReference<MethodReference>()?.returnType == textDataClassType
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
                """
            )
        }

        // endregion

        // region Hook Shorts

        // Filter that parses the video id from the UI
        addLithoFilter(FILTER_CLASS_DESCRIPTOR)

        if (is_20_07_or_greater) {
            // Turn off a/b flag that enables new code for creating litho spans.
            // If enabled then the litho text span hook is never called.
            // Target code is very obfuscated and exactly what the code does is not clear.
            // Return late so debug patch logs if the flag is enabled.
            textComponentFeatureFlagFingerprint.method.returnLate(false)
        }

        // Player response video id is needed to search for the video ids in Shorts litho components.
        hookPlayerResponseVideoId("$FILTER_CLASS_DESCRIPTOR->newPlayerResponseVideoId(Ljava/lang/String;Z)V")

        // endregion

        // region Hook rolling numbers.

        val dislikesIndex = rollingNumberSetterFingerprint.patternMatch!!.endIndex

        rollingNumberSetterFingerprint.method.apply {
            val insertIndex = 1

            val charSequenceInstanceRegister =
                getInstruction<OneRegisterInstruction>(0).registerA
            val charSequenceFieldReference =
                getInstruction<ReferenceInstruction>(dislikesIndex).reference

            val conversionContextRegister = implementation!!.registerCount - parameters.size + 1

            val freeRegister = findFreeRegister(insertIndex, charSequenceInstanceRegister, conversionContextRegister)

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

        rollingNumberMeasureAnimatedTextFingerprint.let {
            // Rolling Number text views use the measured width of the raw string for layout.
            // Modify the measure text calculation to include the left drawable separator if needed.
            val patternMatch = it.patternMatch!!
            // Verify the opcodes are at the start of the method.
            if (patternMatch.startIndex != 0) throw PatchException("Unexpected opcode location")
            val endIndex = patternMatch.endIndex

            it.method.apply {
                val measuredTextWidthRegister = getInstruction<OneRegisterInstruction>(endIndex).registerA

                addInstructions(
                    endIndex + 1,
                    """
                        invoke-static {p1, v$measuredTextWidthRegister}, $EXTENSION_CLASS_DESCRIPTOR->onRollingNumberMeasured(Ljava/lang/String;F)F
                        move-result v$measuredTextWidthRegister
                    """
                )
            }
        }

        // Additional text measurement method. Used if YouTube decides not to animate the likes count
        // and sometimes used for initial video load.
        rollingNumberMeasureStaticLabelFingerprint.match(
            rollingNumberMeasureStaticLabelParentFingerprint.originalClassDef,
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

        arrayOf(
            // The rolling number Span is missing styling since it's initially set as a String.
            // Modify the UI text view and use the styled like/dislike Span.
            // Initial TextView is set in this method.
            rollingNumberTextViewFingerprint.method,
            // Videos less than 24 hours after uploaded, like counts will be updated in real time.
            // Whenever like counts are updated, TextView is set in this method.
            rollingNumberTextViewAnimationUpdateFingerprint.method,
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
                    """
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
