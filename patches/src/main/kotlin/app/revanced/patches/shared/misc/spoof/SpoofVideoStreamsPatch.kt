package app.revanced.patches.shared.misc.spoof

import app.revanced.patcher.extensions.*
import app.revanced.patcher.patch.BytecodePatchBuilder
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.mutable.MutableMethod
import com.android.tools.smali.dexlib2.mutable.MutableMethod.Companion.toMutable

internal const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/shared/spoof/SpoofVideoStreamsPatch;"

private lateinit var buildRequestMethod: MutableMethod
private var buildRequestMethodUrlRegister = -1

internal fun spoofVideoStreamsPatch(
    extensionClassDescriptor: String,
    getMainActivityOnCreateMethod: BytecodePatchContext.() -> MutableMethod,
    fixMediaFetchHotConfig: BytecodePatchBuilder.() -> Boolean = { false },
    fixMediaFetchHotConfigAlternative: BytecodePatchBuilder.() -> Boolean = { false },
    fixParsePlaybackResponseFeatureFlag: BytecodePatchBuilder.() -> Boolean = { false },
    block: BytecodePatchBuilder.() -> Unit,
    executeBlock: BytecodePatchContext.() -> Unit = {},
) = bytecodePatch(
    name = "Spoof video streams", // TODO
    description = "Adds options to spoof the client video streams to fix playback.",
) {
    block()

    dependsOn(addResourcesPatch)

    apply {
        addResources("shared", "misc.fix.playback.spoofVideoStreamsPatch")

        getMainActivityOnCreateMethod().addInstruction(
            0,
            "invoke-static { }, $extensionClassDescriptor->setClientOrderToUse()V",
        )

        // TODO?: Force off 45708738L ?

        // region Enable extension helper method used by other patches

        patchIncludedExtensionMethodMethod.returnEarly(true)

        // endregion

        // region Block /initplayback requests to fall back to /get_watch requests.

        buildInitPlaybackRequestMatch.method.apply {
            val moveUriStringIndex = buildInitPlaybackRequestMatch.indices.first()
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

        buildPlayerRequestURIMethodMatch.method.apply {
            val invokeToStringIndex = buildPlayerRequestURIMethodMatch.indices.first()
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

        buildRequestMethodMatch.method.apply {
            buildRequestMethod = this

            val newRequestBuilderIndex = buildRequestMethodMatch.indices.first()
            buildRequestMethodUrlRegister = getInstruction<FiveRegisterInstruction>(newRequestBuilderIndex).registerD
            val freeRegister = findFreeRegister(newRequestBuilderIndex, buildRequestMethodUrlRegister)

            addInstructions(
                newRequestBuilderIndex,
                """
                    move-object v$freeRegister, p1
                    invoke-static { v$buildRequestMethodUrlRegister, v$freeRegister }, $EXTENSION_CLASS_DESCRIPTOR->fetchStreams(Ljava/lang/String;Ljava/util/Map;)V
                """,
            )
        }

        // endregion

        // region Replace the streaming data with the replacement streams.

        createStreamingDataMethodMatch.method.apply {
            val setStreamDataMethodName = "patch_setStreamingData"
            val resultMethodType = createStreamingDataMethodMatch.classDef.type
            val videoDetailsIndex = createStreamingDataMethodMatch.indices.last()
            val videoDetailsRegister = getInstruction<TwoRegisterInstruction>(videoDetailsIndex).registerA
            val videoDetailsClass = getInstruction(videoDetailsIndex).getReference<FieldReference>()!!.type

            addInstruction(
                videoDetailsIndex + 1,
                "invoke-direct { p0, v$videoDetailsRegister }, " +
                    "$resultMethodType->$setStreamDataMethodName($videoDetailsClass)V",
            )

            val protobufClass = protobufClassParseByteBufferMethod.definingClass
            val setStreamingDataIndex = createStreamingDataMethodMatch.indices.first()

            val playerProtoClass = getInstruction(setStreamingDataIndex + 1)
                .getReference<FieldReference>()!!.definingClass

            val setStreamingDataField = getInstruction(setStreamingDataIndex).getReference<FieldReference>()

            val getStreamingDataField = getInstruction(
                indexOfFirstInstructionOrThrow {
                    opcode == Opcode.IGET_OBJECT && getReference<FieldReference>()?.definingClass == playerProtoClass
                },
            ).getReference<FieldReference>()

            // Use a helper method to avoid the need of picking out multiple free registers from the hooked code.
            createStreamingDataMethodMatch.classDef.methods.add(
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

        // region block getAtt request

        buildRequestMethod.apply {
            val insertIndex = indexOfNewUrlRequestBuilderInstruction(this)

            addInstructions(
                insertIndex,
                """
                    invoke-static { v$buildRequestMethodUrlRegister }, $EXTENSION_CLASS_DESCRIPTOR->blockGetAttRequest(Ljava/lang/String;)Ljava/lang/String;
                    move-result-object v$buildRequestMethodUrlRegister
                """,
            )
        }

        // endregion

        // region Remove /videoplayback request body to fix playback.
        // It is assumed, YouTube makes a request with a body tuned for Android.
        // Requesting streams intended for other platforms with a body tuned for Android could be the cause of 400 errors.
        // A proper fix may include modifying the request body to match the platforms expected body.

        buildMediaDataSourceMethod.apply {
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

        nerdsStatsVideoFormatBuilderMethod.apply {
            findInstructionIndicesReversedOrThrow(Opcode.RETURN_OBJECT).forEach { index ->
                val register = getInstruction<OneRegisterInstruction>(index).registerA

                addInstructions(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS_DESCRIPTOR->appendSpoofedClient(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$register
                    """,
                )
            }
        }

        // endregion

        // region Fix iOS livestream current time.

        hlsCurrentTimeMethodMatch.method.insertLiteralOverride(
            hlsCurrentTimeMethodMatch.indices.first(),
            "$EXTENSION_CLASS_DESCRIPTOR->fixHLSCurrentTime(Z)Z",
        )

        // endregion

        // region Disable SABR playback.
        // If SABR is disabled, it seems 'MediaFetchHotConfig' may no longer need an override (not confirmed).

        val (mediaFetchEnumClass, sabrFieldReference) = with(mediaFetchEnumConstructorMethodMatch.method) {
            val disabledBySABRStreamingUrlString = mediaFetchEnumConstructorMethodMatch.indices.last()

            val mediaFetchEnumClass = definingClass
            val sabrFieldIndex = indexOfFirstInstructionOrThrow(disabledBySABRStreamingUrlString) {
                opcode == Opcode.SPUT_OBJECT &&
                    getReference<FieldReference>()?.type == mediaFetchEnumClass
            }

            Pair(
                mediaFetchEnumClass,
                getInstruction<ReferenceInstruction>(sabrFieldIndex).reference,
            )
        }

        val sabrFingerprint = fingerprint {
            returnType(mediaFetchEnumClass)
            opcodes(
                Opcode.SGET_OBJECT,
                Opcode.RETURN_OBJECT,
            )
            custom { method, _ ->
                !method.parameterTypes.isEmpty()
            }
        }
        sabrFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS_DESCRIPTOR->disableSABR()Z
                move-result v0
                if-eqz v0, :ignore
                sget-object v0, $sabrFieldReference
                return-object v0
                :ignore
                nop
            """,
        )

        // endregion

        // region turn off stream config replacement feature flag.

        if (fixMediaFetchHotConfig()) {
            mediaFetchHotConfigMethodMatch.method.insertLiteralOverride(
                mediaFetchHotConfigMethodMatch.indices.first(),
                "$EXTENSION_CLASS_DESCRIPTOR->useMediaFetchHotConfigReplacement(Z)Z",
            )
        }

        if (fixMediaFetchHotConfigAlternative()) {
            mediaFetchHotConfigAlternativeMethodMatch.method.insertLiteralOverride(
                mediaFetchHotConfigAlternativeMethodMatch.indices.first(),
                "$EXTENSION_CLASS_DESCRIPTOR->useMediaFetchHotConfigReplacement(Z)Z",
            )
        }

        if (fixParsePlaybackResponseFeatureFlag()) {
            playbackStartDescriptorFeatureFlagMethodMatch.method.insertLiteralOverride(
                playbackStartDescriptorFeatureFlagMethodMatch.indices.first(),
                "$EXTENSION_CLASS_DESCRIPTOR->usePlaybackStartFeatureFlag(Z)Z",
            )
        }

        // endregion

        executeBlock()
    }
}
