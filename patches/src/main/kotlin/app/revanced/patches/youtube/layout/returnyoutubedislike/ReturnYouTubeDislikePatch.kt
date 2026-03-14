package app.revanced.patches.youtube.layout.returnyoutubedislike

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.fieldReference
import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.methodReference
import app.revanced.patcher.extensions.typeReference
import app.revanced.patcher.immutableClassDef
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.NonInteractivePreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceCategory
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.shared.misc.litho.filter.addLithoFilter
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.litho.filter.lithoFilterPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.misc.playservice.*
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.patches.youtube.shared.conversionContextToStringMethod
import app.revanced.patches.youtube.shared.rollingNumberTextViewAnimationUpdateMethodMatch
import app.revanced.patches.youtube.video.videoid.hookPlayerResponseVideoId
import app.revanced.patches.youtube.video.videoid.hookVideoId
import app.revanced.patches.youtube.video.videoid.videoIdPatch
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.cloneMutableAndPreserveParameters
import app.revanced.util.findFreeRegister
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.insertLiteralOverride
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/ReturnYouTubeDislikePatch;"

private const val FILTER_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/litho/ReturnYouTubeDislikeFilter;"

@Suppress("ObjectPropertyName")
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
            "20.14.43",
            "20.21.37",
            "20.26.46",
            "20.31.42",
            "20.37.48",
            "20.40.45"
        ),
    )

    apply {
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
                tag = "app.revanced.extension.youtube.returnyoutubedislike.ui.ReturnYouTubeDislikeDebugStatsPreferenceCategory",
            ),
        )

        // region Inject newVideoLoaded event handler to update dislikes when a new video is loaded.

        hookVideoId("$EXTENSION_CLASS_DESCRIPTOR->newVideoLoaded(Ljava/lang/String;)V")

        // Hook the player response video ID, to start loading RYD sooner in the background.
        hookPlayerResponseVideoId("$EXTENSION_CLASS_DESCRIPTOR->preloadVideoId(Ljava/lang/String;Z)V")

        // endregion

        // region Hook like/dislike/remove like button clicks to send votes to the API.

        arrayOf(
            likeMethod to Vote.LIKE,
            dislikeMethod to Vote.DISLIKE,
            removeLikeMethod to Vote.REMOVE_LIKE,
        ).forEach { (method, vote) ->
            method.addInstructions(
                0,
                """
                    const/4 v0, ${vote.value}
                    invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->sendVote(I)V
                """
            )
        }

        // endregion

        // region Hook code for creation and cached lookup of text Spans.

        // Alternatively the hook can be made in the creation of Spans in TextComponentSpec.
        // And it works in all situations except if the likes do not such as disliking.
        // This hook handles all situations, as it's where the created Spans are stored and later reused.

        // Find the field name of the conversion context.
        val conversionContextClass = conversionContextToStringMethod.immutableClassDef
        val textComponentConversionContextField =
            textComponentConstructorMethod.immutableClassDef.fields.find {
                it.type == conversionContextClass.type ||
                        // 20.41+ uses superclass field type.
                        it.type == conversionContextClass.superclass
            } ?: throw PatchException("Could not find conversion context field")

        val conversionContextPathBuilderField = conversionContextToStringMethod.immutableClassDef
            .fields.single { field -> field.type == "Ljava/lang/StringBuilder;" }

        // Old pre 20.40 and lower hook.
        // 21.05 clobbers p0 (this) register.
        // Add additional registers so all parameters including p0 are free to use anywhere in the method.
        textComponentConstructorMethod.immutableClassDef
            .getTextComponentLookupMethod()
            .cloneMutableAndPreserveParameters().apply {
                // Find the instruction for creating the text data object.
                val textDataClassType = textComponentDataMethod.immutableClassDef.type

                val insertIndex: Int
                val charSequenceRegister: Int

                if (is_19_33_or_greater && !is_20_10_or_greater) {
                    val index = indexOfFirstInstructionOrThrow {
                        (opcode == Opcode.INVOKE_STATIC || opcode == Opcode.INVOKE_STATIC_RANGE) &&
                                methodReference?.returnType == textDataClassType
                    }

                    insertIndex = indexOfFirstInstructionOrThrow(index) {
                        opcode == Opcode.INVOKE_VIRTUAL &&
                                methodReference?.parameterTypes?.firstOrNull() == "Ljava/lang/CharSequence;"
                    }

                    charSequenceRegister =
                        getInstruction<FiveRegisterInstruction>(insertIndex).registerD
                } else {
                    insertIndex = indexOfFirstInstructionOrThrow {
                        opcode == Opcode.NEW_INSTANCE &&
                                typeReference?.type == textDataClassType
                    }

                    val charSequenceIndex = indexOfFirstInstructionOrThrow(insertIndex) {
                        opcode == Opcode.IPUT_OBJECT &&
                                fieldReference?.type == "Ljava/lang/CharSequence;"
                    }
                    charSequenceRegister =
                        getInstruction<TwoRegisterInstruction>(charSequenceIndex).registerA
                }

                val conversionContext = findFreeRegister(insertIndex, charSequenceRegister)

                addInstructionsAtControlFlowLabel(
                    insertIndex,
                    """
                    # Copy conversion context.
                    move-object/from16 v$conversionContext, p0
                    iget-object v$conversionContext, v$conversionContext, $textComponentConversionContextField
                    invoke-static { v$conversionContext, v$charSequenceRegister }, $EXTENSION_CLASS_DESCRIPTOR->onLithoTextLoaded(Ljava/lang/Object;Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                    move-result-object v$charSequenceRegister
                    
                    :ignore
                    nop
                """
                )
            }

        // Hook new litho text creation code.
        if (is_20_07_or_greater) {
            textComponentFeatureFlagMethodMatch.let {
                it.method.insertLiteralOverride(
                    it[0],
                    "$EXTENSION_CLASS_DESCRIPTOR->useNewLithoTextCreation(Z)Z"
                )
            }

            lithoSpannableStringCreationMethodMatch.let {
                val conversionContextField = it.immutableClassDef.type +
                        "->" + textComponentConversionContextField.name +
                        ":" + textComponentConversionContextField.type

                it.method.apply {
                    val insertIndex = it[1]
                    val charSequenceRegister =
                        getInstruction<FiveRegisterInstruction>(insertIndex).registerD
                    val conversionContextPathRegister =
                        findFreeRegister(insertIndex, charSequenceRegister)

                    addInstructions(
                        insertIndex,
                        """
                            move-object/from16 v$conversionContextPathRegister, p0
                            iget-object v$conversionContextPathRegister, v$conversionContextPathRegister, $conversionContextField
                            iget-object v$conversionContextPathRegister, v$conversionContextPathRegister, $conversionContextPathBuilderField
                            invoke-static { v$conversionContextPathRegister, v$charSequenceRegister }, $EXTENSION_CLASS_DESCRIPTOR->onLithoTextLoaded(Ljava/lang/Object;Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
                            move-result-object v$charSequenceRegister
                        """
                    )
                }
            }
        }

        // endregion

        // region Hook Shorts

        // Filter that parses the video ID from the UI
        addLithoFilter(FILTER_CLASS_DESCRIPTOR)

        // Player response video ID is needed to search for the video IDs in Shorts litho components.
        hookPlayerResponseVideoId("$FILTER_CLASS_DESCRIPTOR->newPlayerResponseVideoId(Ljava/lang/String;Z)V")

        // endregion

        // region Hook rolling numbers.

        rollingNumberSetterMethodMatch.method.apply {
            val insertIndex = 1
            val dislikesIndex = rollingNumberSetterMethodMatch[-1]
            val charSequenceInstanceRegister = getInstruction<OneRegisterInstruction>(0).registerA
            val charSequenceFieldReference =
                getInstruction<ReferenceInstruction>(dislikesIndex).reference

            val conversionContextRegister = implementation!!.registerCount - parameters.size + 1

            val freeRegister = findFreeRegister(
                insertIndex,
                charSequenceInstanceRegister,
                conversionContextRegister
            )

            addInstructions(
                insertIndex,
                """
                    iget-object v$freeRegister, v$charSequenceInstanceRegister, $charSequenceFieldReference
                    invoke-static { v$conversionContextRegister, v$freeRegister }, $EXTENSION_CLASS_DESCRIPTOR->onRollingNumberLoaded(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$freeRegister
                    iput-object v$freeRegister, v$charSequenceInstanceRegister, $charSequenceFieldReference
                """,
            )
        }

        // Rolling Number text views use the measured width of the raw string for layout.
        // Modify the measure text calculation to include the left drawable separator if needed.
        rollingNumberMeasureAnimatedTextMethodMatch.let {
            // Additional check to verify the opcodes are at the start of the method
            if (it[0] != 0) throw PatchException("Unexpected opcode location")
            val endIndex = it[-1]

            it.method.apply {
                val measuredTextWidthRegister =
                    getInstruction<OneRegisterInstruction>(endIndex).registerA

                addInstructions(
                    endIndex + 1,
                    """
                        invoke-static { p1, v$measuredTextWidthRegister }, $EXTENSION_CLASS_DESCRIPTOR->onRollingNumberMeasured(Ljava/lang/String;F)F
                        move-result v$measuredTextWidthRegister
                    """,
                )
            }
        }

        // Additional text measurement method. Used if YouTube decides not to animate the likes count
        // and sometimes used for initial video load.
        rollingNumberMeasureStaticLabelParentMethod.immutableClassDef.rollingNumberMeasureStaticLabelMethodMatch.let {
            val measureTextIndex = it[0] + 1
            it.method.apply {
                val freeRegister = getInstruction<TwoRegisterInstruction>(0).registerA

                addInstructions(
                    measureTextIndex + 1,
                    """
                        move-result v$freeRegister
                        invoke-static { p1, v$freeRegister }, $EXTENSION_CLASS_DESCRIPTOR->onRollingNumberMeasured(Ljava/lang/String;F)F
                    """,
                )
            }
        }

        arrayOf(
            // The rolling number Span is missing styling since it's initially set as a String.
            // Modify the UI text view and use the styled like/dislike Span.
            // Initial TextView is set in this method.
            rollingNumberTextViewMethod,
            // Videos less than 24 hours after uploaded, like counts will be updated in real time.
            // Whenever like counts are updated, TextView is set in this method.
            rollingNumberTextViewAnimationUpdateMethodMatch.method,
        ).forEach { insertMethod ->
            insertMethod.apply {
                val setTextIndex = indexOfFirstInstructionOrThrow {
                    methodReference?.name == "setText"
                }

                val textViewRegister =
                    getInstruction<FiveRegisterInstruction>(setTextIndex).registerC
                val textSpanRegister =
                    getInstruction<FiveRegisterInstruction>(setTextIndex).registerD

                addInstructions(
                    setTextIndex,
                    """
                        invoke-static { v$textViewRegister, v$textSpanRegister }, $EXTENSION_CLASS_DESCRIPTOR->updateRollingNumber(Landroid/widget/TextView;Ljava/lang/CharSequence;)Ljava/lang/CharSequence;
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
