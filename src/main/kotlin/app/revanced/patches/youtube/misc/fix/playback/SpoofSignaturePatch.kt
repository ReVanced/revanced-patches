package app.revanced.patches.youtube.misc.fix.playback

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patcher.util.smali.ExternalLabel
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.*
import app.revanced.patches.youtube.misc.integrations.IntegrationsPatch
import app.revanced.patches.youtube.misc.playertype.PlayerTypeHookPatch
import app.revanced.patches.youtube.video.information.VideoInformationPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    description = "Spoofs the signature to prevent playback issues.",
    dependencies = [
        SpoofSignatureResourcePatch::class,
        IntegrationsPatch::class,
        PlayerTypeHookPatch::class,
        VideoInformationPatch::class
    ]
)
object SpoofSignaturePatch : BytecodePatch(
    setOf(
        ProtobufParameterBuilderFingerprint,
        StoryboardThumbnailParentFingerprint,
        StoryboardRendererSpecFingerprint,
        PlayerResponseModelImplFingerprint
    )
) {
    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "Lapp/revanced/integrations/patches/spoof/SpoofSignaturePatch;"

    override fun execute(context: BytecodeContext) {
        // Hook parameter.
        ProtobufParameterBuilderFingerprint.result?.let {
            val setParamMethod = context
                .toMethodWalker(it.method)
                .nextMethod(it.scanResult.patternScanResult!!.startIndex, true).getMethod() as MutableMethod

            setParamMethod.apply {
                val protobufParameterRegister = 3

                addInstructions(
                    0,
                    """
                        invoke-static {p$protobufParameterRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->spoofParameter(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object p$protobufParameterRegister
                    """
                )
            }
        } ?: throw ProtobufParameterBuilderFingerprint.exception

        // When signature spoofing is enabled, the seekbar when tapped does not show
        // the video time, chapter names, or the video thumbnail.
        // Changing the value returned of this method forces all of these to show up,
        // except the thumbnails are blank, which is handled with the patch below.
        StoryboardThumbnailParentFingerprint.result?.classDef?.let { classDef ->
            StoryboardThumbnailFingerprint.also {
                it.resolve(
                    context,
                    classDef
                )
            }.result?.let {
                val endIndex = it.scanResult.patternScanResult!!.endIndex
                // Replace existing instruction to preserve control flow label.
                // The replaced return instruction always returns false
                // (it is the 'no thumbnails found' control path),
                // so there is no need to pass the existing return value to integrations.
                it.mutableMethod.replaceInstruction(
                    endIndex,
                    """
                        invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->getSeekbarThumbnailOverrideValue()Z
                    """
                )
                // Since this is end of the method must replace one line then add the rest.
                it.mutableMethod.addInstructions(
                    endIndex + 1,
                    """
                    move-result v0
                    return v0
                """
                )
            } ?: throw StoryboardThumbnailFingerprint.exception

            /**
             * Hook StoryBoard renderer url
             */
            PlayerResponseModelImplFingerprint.result?.let {
                it.mutableMethod.apply {
                    val getStoryBoardIndex = it.scanResult.patternScanResult!!.endIndex
                    val getStoryBoardRegister = getInstruction<OneRegisterInstruction>(getStoryBoardIndex).registerA

                    addInstructions(
                        getStoryBoardIndex,
                        """
                        invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->getStoryboardRendererSpec()Ljava/lang/String;
                        move-result-object v$getStoryBoardRegister
                    """
                    )
                }
            } ?: throw PlayerResponseModelImplFingerprint.exception

            StoryboardRendererSpecFingerprint.result?.let {
                it.mutableMethod.apply {
                    val storyBoardUrlParams = 0

                    addInstructionsWithLabels(
                        0,
                        """
                        if-nez p$storyBoardUrlParams, :ignore
                        invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->getStoryboardRendererSpec()Ljava/lang/String;
                        move-result-object p$storyBoardUrlParams
                    """,
                        ExternalLabel("ignore", getInstruction(0))
                    )
                }
            } ?: throw StoryboardRendererSpecFingerprint.exception
        }
    }
}