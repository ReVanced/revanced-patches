package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.NonInteractivePreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/youtube/patches/spoof/SpoofVideoStreamsPatch;"

@Suppress("unused")
val spoofVideoStreamsPatch = bytecodePatch(
    name = "Spoof video streams",
    description = "Spoofs the client video streams to allow video playback.",
) {
    compatibleWith(
        "com.google.android.youtube"(
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
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
            "19.12.41",
            "19.13.37",
            "19.14.43",
            "19.15.36",
            "19.16.39",
        ),
    )

    dependsOn(
        settingsPatch,
        addResourcesPatch,
        userAgentClientSpoofPatch,
    )

    val buildInitPlaybackRequestMatch by buildInitPlaybackRequestFingerprint()
    val buildPlayerRequestURIMatch by buildPlayerRequestURIFingerprint()
    val createStreamingDataMatch by createStreamingDataFingerprint()
    val buildMediaDataSourceMatch by buildMediaDataSourceFingerprint()
    val buildRequestMatch by buildRequestFingerprint()
    val protobufClassParseByteBufferMatch by protobufClassParseByteBufferFingerprint()

    execute {
        addResources("youtube", "misc.fix.playback.spoofVideoStreamsPatch")

        PreferenceScreen.MISC.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_spoof_video_streams_screen",
                sorting = PreferenceScreenPreference.Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_spoof_video_streams"),
                    ListPreference(
                        "revanced_spoof_video_streams_client",
                        summaryKey = null,
                    ),
                    SwitchPreference(
                        "revanced_spoof_video_streams_ios_force_avc",
                        tag = "app.revanced.extension.youtube.settings.preference.ForceAVCSpoofingPreference",
                    ),
                    NonInteractivePreference("revanced_spoof_video_streams_about_android_vr"),
                    NonInteractivePreference("revanced_spoof_video_streams_about_ios"),
                ),
            ),
        )

        // region Block /initplayback requests to fall back to /get_watch requests.

        val moveUriStringIndex = buildInitPlaybackRequestMatch.patternMatch!!.startIndex

        buildInitPlaybackRequestMatch.mutableMethod.apply {
            val targetRegister = getInstruction<OneRegisterInstruction>(moveUriStringIndex).registerA

            addInstructions(
                moveUriStringIndex + 1,
                """
                    invoke-static { v$targetRegister }, $EXTENSION_CLASS_DESCRIPTOR->blockInitPlaybackRequest(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$targetRegister
                """,
            )
        }

        // endregion

        // region Block /get_watch requests to fall back to /player requests.

        val invokeToStringIndex = buildPlayerRequestURIMatch.patternMatch!!.startIndex

        buildPlayerRequestURIMatch.mutableMethod.apply {
            val uriRegister = getInstruction<FiveRegisterInstruction>(invokeToStringIndex).registerC

            addInstructions(
                invokeToStringIndex,
                """
                    invoke-static { v$uriRegister }, $EXTENSION_CLASS_DESCRIPTOR->blockGetWatchRequest(Landroid/net/Uri;)Landroid/net/Uri;
                    move-result-object v$uriRegister
                """,
            )
        }

        // endregion

        // region Get replacement streams at player requests.

        buildRequestMatch.mutableMethod.apply {
            val newRequestBuilderIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_VIRTUAL &&
                    getReference<MethodReference>()?.name == "newUrlRequestBuilder"
            }
            val urlRegister = getInstruction<FiveRegisterInstruction>(newRequestBuilderIndex).registerD
            val freeRegister = getInstruction<OneRegisterInstruction>(newRequestBuilderIndex + 1).registerA

            addInstructions(
                newRequestBuilderIndex,
                """
                move-object v$freeRegister, p1
                invoke-static { v$urlRegister, v$freeRegister }, $EXTENSION_CLASS_DESCRIPTOR->fetchStreams(Ljava/lang/String;Ljava/util/Map;)V
            """,
            )
        }

        // endregion

        // region Replace the streaming data with the replacement streams.

        createStreamingDataMatch.mutableMethod.apply {
            val setStreamDataMethodName = "patch_setStreamingData"
            val resultMethodType = createStreamingDataMatch.mutableClass.type
            val videoDetailsIndex = createStreamingDataMatch.patternMatch!!.endIndex
            val videoDetailsRegister = getInstruction<TwoRegisterInstruction>(videoDetailsIndex).registerA
            val videoDetailsClass = getInstruction(videoDetailsIndex).getReference<FieldReference>()!!.type

            addInstruction(
                videoDetailsIndex + 1,
                "invoke-direct { p0, v$videoDetailsRegister }, " +
                    "$resultMethodType->$setStreamDataMethodName($videoDetailsClass)V",
            )

            val protobufClass = protobufClassParseByteBufferMatch.mutableMethod.definingClass
            val setStreamingDataIndex = createStreamingDataMatch.patternMatch!!.startIndex

            val playerProtoClass = getInstruction(setStreamingDataIndex + 1)
                .getReference<FieldReference>()!!.definingClass

            val setStreamingDataField = getInstruction(setStreamingDataIndex).getReference<FieldReference>()

            val getStreamingDataField = getInstruction(
                indexOfFirstInstructionOrThrow {
                    opcode == Opcode.IGET_OBJECT && getReference<FieldReference>()?.definingClass == playerProtoClass
                },
            ).getReference<FieldReference>()

            // Use a helper method to avoid the need of picking out multiple free registers from the hooked code.
            createStreamingDataMatch.mutableClass.methods.add(
                ImmutableMethod(
                    resultMethodType,
                    setStreamDataMethodName,
                    listOf(ImmutableMethodParameter(videoDetailsClass, null, "videoDetails")),
                    "V",
                    AccessFlags.PRIVATE.value or AccessFlags.FINAL.value,
                    null,
                    null,
                    MutableMethodImplementation(9),
                ).toMutable().apply {
                    addInstructionsWithLabels(
                        0,
                        """
                            invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->isSpoofingEnabled()Z
                            move-result v0
                            if-eqz v0, :disabled
    
                            # Get video id.
                            iget-object v2, p1, $videoDetailsClass->c:Ljava/lang/String;
                            if-eqz v2, :disabled
    
                            # Get streaming data.
                            invoke-static { v2 }, $EXTENSION_CLASS_DESCRIPTOR->getStreamingData(Ljava/lang/String;)Ljava/nio/ByteBuffer;
                            move-result-object v3
                            if-eqz v3, :disabled
    
                            # Parse streaming data.
                            sget-object v4, $playerProtoClass->a:$playerProtoClass
                            invoke-static { v4, v3 }, $protobufClass->parseFrom(${protobufClass}Ljava/nio/ByteBuffer;)$protobufClass
                            move-result-object v5
                            check-cast v5, $playerProtoClass
    
                            # Set streaming data.
                            iget-object v6, v5, $getStreamingDataField
                            if-eqz v6, :disabled
                            iput-object v6, p0, $setStreamingDataField
                            
                            :disabled
                            return-void
                        """,
                    )
                },
            )
        }

        // endregion

        // region Remove /videoplayback request body to fix playback.
        // This is needed when using iOS client as streaming data source.

        buildMediaDataSourceMatch.mutableMethod.apply {
            val targetIndex = instructions.lastIndex

            // Instructions are added just before the method returns,
            // so there's no concern of clobbering in-use registers.
            addInstructions(
                targetIndex,
                """
                        # Field a: Stream uri.
                        # Field c: Http method.
                        # Field d: Post data.
                        move-object v0, p0  # method has over 15 registers and must copy p0 to a lower register.
                        iget-object v1, v0, $definingClass->a:Landroid/net/Uri;
                        iget v2, v0, $definingClass->c:I
                        iget-object v3, v0, $definingClass->d:[B
                        invoke-static { v1, v2, v3 }, $EXTENSION_CLASS_DESCRIPTOR->removeVideoPlaybackPostBody(Landroid/net/Uri;I[B)[B
                        move-result-object v1
                        iput-object v1, v0, $definingClass->d:[B
                    """,
            )
        }
        // endregion
    }
}
