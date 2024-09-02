package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.all.misc.resources.AddResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreen
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.BuildBrowseRequestFingerprint
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.BuildInitPlaybackRequestFingerprint
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.BuildMediaDataSourceFingerprint
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.CreateStreamingDataFingerprint
import app.revanced.patches.youtube.misc.settings.SettingsPatch
import app.revanced.patches.youtube.video.videoid.VideoIdPatch
import app.revanced.util.getReference
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Patch(
    name = "Spoof client",
    description = "Spoofs the client to allow video playback.",
    dependencies = [
        SettingsPatch::class,
        AddResourcesPatch::class,
        UserAgentClientSpoofPatch::class,
        VideoIdPatch::class,
    ],
    compatiblePackages = [
        CompatiblePackage(
            "com.google.android.youtube",
            [
                // This patch supports these version, but VideoIdPatch does not.
                // "18.37.36",
                // "18.38.44",
                // "18.43.45",
                // "18.44.41",
                // "18.45.43",
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
            ],
        ),
    ],
)
object SpoofClientPatch : BytecodePatch(
    setOf(
        BuildInitPlaybackRequestFingerprint,
        BuildBrowseRequestFingerprint,
        CreateStreamingDataFingerprint,
        BuildMediaDataSourceFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/youtube/patches/spoof/SpoofClientPatch;"
    private const val STREAMING_DATA_CLASS_DESCRIPTOR =
        "Lcom/google/protos/youtube/api/innertube/StreamingDataOuterClass\$StreamingData;"

    override fun execute(context: BytecodeContext) {
        AddResourcesPatch(this::class)

        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            PreferenceScreen(
                key = "revanced_spoof_client_screen",
                sorting = PreferenceScreen.Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_spoof_client"),
                    SwitchPreference("revanced_spoof_client_force_avc"),
                )
            )
        )

        // Prefetch streaming data.
        VideoIdPatch.hookPlayerResponseVideoId("$INTEGRATIONS_CLASS_DESCRIPTOR->fetchStreamingData(Ljava/lang/String;Z)V")
        
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

        // region Copy request headers for streaming data fetch.

        BuildBrowseRequestFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val newRequestBuilderIndex = it.scanResult.patternScanResult!!.endIndex
                val urlRegister = getInstruction<FiveRegisterInstruction>(newRequestBuilderIndex).registerD

                addInstruction(
                    newRequestBuilderIndex,
                    """
                        invoke-static { v$urlRegister, p1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->setFetchHeaders(Ljava/lang/String;Ljava/util/Map;)V
                    """
                )
            }
        }

        // endregion

        // region Replace the streaming data.

        CreateStreamingDataFingerprint.resultOrThrow().let { result ->
            result.mutableMethod.apply {
                val videoDetailsIndex = result.scanResult.patternScanResult!!.endIndex
                val videoDetailsClass = getInstruction(videoDetailsIndex).getReference<FieldReference>()!!.type
                val playerProtoClass = parameterTypes.first()
                val protobufClass = getInstructions().find { instruction ->
                    instruction.opcode == Opcode.INVOKE_STATIC &&
                    instruction.getReference<MethodReference>()!!.name.endsWith("smcheckIsLite")
                }!!.getReference<MethodReference>()!!.definingClass

                addInstructionsWithLabels(
                    videoDetailsIndex + 1,
                    """
                        # Registers is free at this index.

                        invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->isSpoofingEnabled()Z
                        move-result v1
                        if-eqz v1, :disabled

                        # Get video id.
                        iget-object v1, v0, $videoDetailsClass->c:Ljava/lang/String;
                        if-eqz v1, :disabled

                        # Get streaming data.
                        invoke-static { v1 }, $INTEGRATIONS_CLASS_DESCRIPTOR->getStreamingData(Ljava/lang/String;)Ljava/nio/ByteBuffer;
                        move-result-object v1
                        if-eqz v1, :disabled

                        # Parse streaming data.
                        sget-object v0, $playerProtoClass->a:$playerProtoClass
                        invoke-static { v0, v1 }, $protobufClass->parseFrom(${protobufClass}Ljava/nio/ByteBuffer;)$protobufClass
                        move-result-object v1
                        check-cast v1, $playerProtoClass

                        # Set streaming data.
                        iget-object v0, v1, $playerProtoClass->h:$STREAMING_DATA_CLASS_DESCRIPTOR
                        if-eqz v0, :disabled
                        iput-object v0, p0, $definingClass->a:$STREAMING_DATA_CLASS_DESCRIPTOR
                    """,
                    ExternalLabel("disabled", getInstruction(videoDetailsIndex + 1))
                )
            }
        }

        // endregion

        // region Remove /videoplayback request body to fix playback.
        // This is needed when using iOS client as streaming data source.

        BuildMediaDataSourceFingerprint.resultOrThrow().let {
            it.mutableMethod.apply {
                val targetIndex = getInstructions().lastIndex

                addInstructions(
                    targetIndex,
                    """
                        # Field a: Stream uri.
                        # Field c: Http method.
                        # Field d: Post data.
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
