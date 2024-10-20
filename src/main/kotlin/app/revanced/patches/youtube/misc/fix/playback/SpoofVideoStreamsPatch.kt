package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.extensions.or
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.ListPreference
import app.revanced.patches.shared.misc.settings.preference.NonInteractivePreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.BuildInitPlaybackRequestFingerprint
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.BuildMediaDataSourceFingerprint
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.BuildPlayerRequestURIFingerprint
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.BuildRequestFingerprint
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.CreateStreamingDataFingerprint
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.ProtobufClassParseByteBufferFingerprint
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.resultOrThrow
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

@Patch(
    name = "Spoof video streams",
    description = "Spoofs the client video streams to allow video playback.",
    dependencies = [
        SettingsPatch::class,
        AddResourcesPatch::class,
        UserAgentClientSpoofPatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                "18.38.44",
                "18.49.37",
                "19.16.39",
                "19.25.37",
                "19.34.42",
            ],
        ),
    ],
)
object SpoofVideoStreamsPatch : BytecodePatch(
    setOf(
        BuildInitPlaybackRequestFingerprint,
        BuildPlayerRequestURIFingerprint,
        CreateStreamingDataFingerprint,
        BuildMediaDataSourceFingerprint,
        BuildRequestFingerprint,
        ProtobufClassParseByteBufferFingerprint,
    ),
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/spoof/SpoofVideoStreamsPatch;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            PreferenceScreen(
                key = "revanced_spoof_video_streams_screen",
                sorting = PreferenceScreen.Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_spoof_video_streams"),
                    ListPreference(
                        "revanced_spoof_video_streams_client",
                        summaryKey = null
                    ),
                    SwitchPreference(
                        "revanced_spoof_video_streams_ios_force_avc",
                        tag = "app.revanced.integrations.youtube.settings.preference.ForceAVCSpoofingPreference",
                    ),
                    NonInteractivePreference("revanced_spoof_video_streams_about_android_vr"),
                    NonInteractivePreference("revanced_spoof_video_streams_about_ios"),
                ),
            ),
        )

        // region Block /initplayback requests to fall back to /get_watch requests.

        BuildInitPlaybackRequestFingerprint.resultOrThrow().let {
            val moveUriStringIndex = it.scanResult.patternScanResult!!.startIndex

            it.mutableMethod.apply {
                val targetRegister = getInstruction<OneRegisterInstruction>(moveUriStringIndex).registerA

                addInstructions(
                    moveUriStringIndex + 1,
                    """
                        invoke-static { v$targetRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->blockInitPlaybackRequest(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object v$targetRegister
                    """,
                )
            }
        }

        // endregion

        // region Block /get_watch requests to fall back to /player requests.

        BuildPlayerRequestURIFingerprint.resultOrThrow().let {
            val invokeToStringIndex = it.scanResult.patternScanResult!!.startIndex

            it.mutableMethod.apply {
                val uriRegister = getInstruction<FiveRegisterInstruction>(invokeToStringIndex).registerC

                addInstructions(
                    invokeToStringIndex,
                    """
                        invoke-static { v$uriRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->blockGetWatchRequest(Landroid/net/Uri;)Landroid/net/Uri;
                        move-result-object v$uriRegister
                    """,
                )
            }
        }

        // endregion

        // region Get replacement streams at player requests.

        BuildRequestFingerprint.resultOrThrow().mutableMethod.apply {
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
                    invoke-static { v$urlRegister, v$freeRegister }, $INTEGRATIONS_CLASS_DESCRIPTOR->fetchStreams(Ljava/lang/String;Ljava/util/Map;)V
                """,
            )
        }

        // endregion

        // region Replace the streaming data with the replacement streams.

        CreateStreamingDataFingerprint.resultOrThrow().let { result ->
            result.mutableMethod.apply {
                val setStreamDataMethodName = "patch_setStreamingData"
                val resultMethodType = result.mutableClass.type
                val videoDetailsIndex = result.scanResult.patternScanResult!!.endIndex
                val videoDetailsRegister = getInstruction<TwoRegisterInstruction>(videoDetailsIndex).registerA
                val videoDetailsClass = getInstruction(videoDetailsIndex).getReference<FieldReference>()!!.type

                addInstruction(
                    videoDetailsIndex + 1,
                    "invoke-direct { p0, v$videoDetailsRegister }, " +
                        "$resultMethodType->$setStreamDataMethodName($videoDetailsClass)V",
                )

                val protobufClass = ProtobufClassParseByteBufferFingerprint.resultOrThrow().mutableMethod.definingClass
                val setStreamingDataIndex = result.scanResult.patternScanResult!!.startIndex

                val playerProtoClass = getInstruction(setStreamingDataIndex + 1)
                    .getReference<FieldReference>()!!.definingClass

                val setStreamingDataField = getInstruction(setStreamingDataIndex).getReference<FieldReference>()

                val getStreamingDataField = getInstruction(
                    indexOfFirstInstructionOrThrow {
                        opcode == Opcode.IGET_OBJECT && getReference<FieldReference>()?.definingClass == playerProtoClass
                    }
                ).getReference<FieldReference>()

                // Use a helper method to avoid the need of picking out multiple free registers from the hooked code.
                result.mutableClass.methods.add(
                    ImmutableMethod(
                        resultMethodType,
                        setStreamDataMethodName,
                        listOf(ImmutableMethodParameter(videoDetailsClass, null, "videoDetails")),
                        "V",
                        AccessFlags.PRIVATE or AccessFlags.FINAL,
                        null,
                        null,
                        MutableMethodImplementation(9),
                    ).toMutable().apply {
                        addInstructionsWithLabels(
                            0,
                            """
                            invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->isSpoofingEnabled()Z
                            move-result v0
                            if-eqz v0, :disabled
    
                            # Get video id.
                            iget-object v2, p1, $videoDetailsClass->c:Ljava/lang/String;
                            if-eqz v2, :disabled
    
                            # Get streaming data.
                            invoke-static { v2 }, $INTEGRATIONS_CLASS_DESCRIPTOR->getStreamingData(Ljava/lang/String;)Ljava/nio/ByteBuffer;
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
        }

        // endregion

        // region Remove /videoplayback request body to fix playback.
        // It is assumed, YouTube makes a request with a body tuned for Android.
        // Requesting streams intended for other platforms with a body tuned for Android could be the cause of 400 errors.
        // A proper fix may include modifying the request body to match the platforms expected body.

        BuildMediaDataSourceFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val targetIndex = getInstructions().lastIndex

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
                        invoke-static { v1, v2, v3 }, $INTEGRATIONS_CLASS_DESCRIPTOR->removeVideoPlaybackPostBody(Landroid/net/Uri;I[B)[B
                        move-result-object v1
                        iput-object v1, v0, $definingClass->d:[B
                    """,
                )
            }
        }

        // endregion
    }
}
