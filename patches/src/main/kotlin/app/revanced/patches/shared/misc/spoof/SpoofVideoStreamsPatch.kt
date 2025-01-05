package app.revanced.patches.shared.misc.spoof

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.insertFeatureFlagBooleanOverride
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/shared/spoof/SpoofVideoStreamsPatch;"

fun spoofVideoStreamsPatch(
    block: BytecodePatchBuilder.() -> Unit = {},
    executeBlock: BytecodePatchContext.() -> Unit = {},
) = bytecodePatch(
    name = "Spoof video streams",
    description = "Spoofs the client video streams to fix playback.",
) {
    block()

    dependsOn(addResourcesPatch)

    execute {
        // region Enable extension helper method used by other patches

        patchIncludedExtensionMethodFingerprint.method.returnEarly(true)

        // endregion

        // region Block /initplayback requests to fall back to /get_watch requests.

        val moveUriStringIndex = buildInitPlaybackRequestFingerprint.filterMatches.first().index

        buildInitPlaybackRequestFingerprint.method.apply {
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

        val invokeToStringIndex = buildPlayerRequestURIFingerprint.patternMatch.startIndex

        buildPlayerRequestURIFingerprint.method.apply {
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

        buildRequestFingerprint.let {
            it.method.apply {
                val builderIndex = it.filterMatches.first().index
                val urlRegister = getInstruction<FiveRegisterInstruction>(builderIndex).registerD
                val freeRegister = getInstruction<OneRegisterInstruction>(builderIndex + 1).registerA

                addInstructions(
                    builderIndex,
                    """
                        move-object v$freeRegister, p1
                        invoke-static { v$urlRegister, v$freeRegister }, $EXTENSION_CLASS_DESCRIPTOR->fetchStreams(Ljava/lang/String;Ljava/util/Map;)V
                    """
                )
            }
        }

        // endregion

        // region Replace the streaming data with the replacement streams.

        createStreamingDataFingerprint.method.apply {
            val setStreamDataMethodName = "patch_setStreamingData"
            val resultMethodType = createStreamingDataFingerprint.classDef.type
            val videoDetailsIndex = createStreamingDataFingerprint.patternMatch.endIndex
            val videoDetailsRegister = getInstruction<TwoRegisterInstruction>(videoDetailsIndex).registerA
            val videoDetailsClass = getInstruction(videoDetailsIndex).getReference<FieldReference>()!!.type

            addInstruction(
                videoDetailsIndex + 1,
                "invoke-direct { p0, v$videoDetailsRegister }, " +
                    "$resultMethodType->$setStreamDataMethodName($videoDetailsClass)V",
            )

            val protobufClass = protobufClassParseByteBufferFingerprint.method.definingClass
            val setStreamingDataIndex = createStreamingDataFingerprint.patternMatch.startIndex

            val playerProtoClass = getInstruction(setStreamingDataIndex + 1)
                .getReference<FieldReference>()!!.definingClass

            val setStreamingDataField = getInstruction(setStreamingDataIndex).getReference<FieldReference>()

            val getStreamingDataField = getInstruction(
                indexOfFirstInstructionOrThrow {
                    opcode == Opcode.IGET_OBJECT && getReference<FieldReference>()?.definingClass == playerProtoClass
                },
            ).getReference<FieldReference>()

            // Use a helper method to avoid the need of picking out multiple free registers from the hooked code.
            createStreamingDataFingerprint.classDef.methods.add(
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
        // It is assumed, YouTube makes a request with a body tuned for Android.
        // Requesting streams intended for other platforms with a body tuned for Android could be the cause of 400 errors.
        // A proper fix may include modifying the request body to match the platforms expected body.

        buildMediaDataSourceFingerprint.method.apply {
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

        // region Append spoof info.

        nerdsStatsVideoFormatBuilderFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN_OBJECT).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                addInstructions(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->appendSpoofedClient(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$register
                    """
                )
            }
        }

        // endregion

        // region Fix iOS livestream current time.

        hlsCurrentTimeFingerprint.let {
            it.method.insertFeatureFlagBooleanOverride(
                it.filterMatches.first().index,
                "$EXTENSION_CLASS_DESCRIPTOR->fixHLSCurrentTime(Z)Z"
            )
        }

        // endregion

        executeBlock()
    }
}
