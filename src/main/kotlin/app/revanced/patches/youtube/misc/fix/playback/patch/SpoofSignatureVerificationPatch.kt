package app.revanced.patches.youtube.misc.fix.playback.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.data.toMethodWalker
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.misc.fix.playback.fingerprints.*
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.playertype.patch.PlayerTypeHookPatch
import org.jf.dexlib2.iface.instruction.ReferenceInstruction

@Name("Spoof signature verification")
@Description("Spoofs a patched client to prevent playback issues.")
@DependsOn([
    SpoofSignatureVerificationResourcePatch::class,
    IntegrationsPatch::class,
    PlayerTypeHookPatch::class
])
@Version("0.0.1")
class SpoofSignatureVerificationPatch : BytecodePatch(
    listOf(
        ProtobufParameterBuilderFingerprint,
        StoryboardThumbnailParentFingerprint,
        ScrubbedPreviewLayoutFingerprint,
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {

        // hook parameter
        ProtobufParameterBuilderFingerprint.result?.let {
            val setParamMethod = context
                .toMethodWalker(it.method)
                .nextMethod(it.scanResult.patternScanResult!!.startIndex, true).getMethod() as MutableMethod

            setParamMethod.apply {
                val protobufParameterRegister = 3

                addInstructions(
                    0,
                    """
                        invoke-static {p$protobufParameterRegister}, $INTEGRATIONS_CLASS_DESCRIPTOR->overrideProtobufParameter(Ljava/lang/String;)Ljava/lang/String;
                        move-result-object p$protobufParameterRegister
                    """
                )
            }
        } ?: return ProtobufParameterBuilderFingerprint.toErrorResult()


        // When signature spoofing is enabled, the seekbar when tapped does not show
        // the video time, chapter names, or the video thumbnail.
        // Changing the value returned of this method forces all of these to show up,
        // except the thumbnails are blank, which is handled with the patch below.
        StoryboardThumbnailParentFingerprint.result ?: return StoryboardThumbnailParentFingerprint.toErrorResult()
        StoryboardThumbnailFingerprint.resolve(context, StoryboardThumbnailParentFingerprint.result!!.classDef)
        StoryboardThumbnailFingerprint.result?.apply {
            val endIndex = scanResult.patternScanResult!!.endIndex
            // Replace existing instruction to preserve control flow label.
            // The replaced return instruction always returns false
            // (it is the 'no thumbnails found' control path),
            // so there is no need to pass the existing return value to integrations.
            mutableMethod.replaceInstruction(
                endIndex,
                """
                    invoke-static {}, $INTEGRATIONS_CLASS_DESCRIPTOR->getSeekbarThumbnailOverrideValue()Z
                """
            )
            // Since this is end of the method must replace one line then add the rest.
            mutableMethod.addInstructions(
                endIndex + 1,
                """
                move-result v0
                return v0
            """
            )
        } ?: return StoryboardThumbnailFingerprint.toErrorResult()


        // Seekbar thumbnail now show up but are always a blank image.
        // Additional changes are needed to force the client to generate the thumbnails (assuming it's possible),
        // but for now hide the empty thumbnail.
        ScrubbedPreviewLayoutFingerprint.result?.apply {
            val endIndex = scanResult.patternScanResult!!.endIndex
            mutableMethod.apply {
                val imageViewFieldName = getInstruction<ReferenceInstruction>(endIndex).reference
                addInstructions(
                    implementation!!.instructions.lastIndex,
                    """
                        iget-object v0, p0, $imageViewFieldName   # copy imageview field to a register
                        invoke-static {v0}, $INTEGRATIONS_CLASS_DESCRIPTOR->seekbarImageViewCreated(Landroid/widget/ImageView;)V
                """
                )
            }
        } ?: return ScrubbedPreviewLayoutFingerprint.toErrorResult()

        return PatchResultSuccess()
    }

    private companion object {
        const val INTEGRATIONS_CLASS_DESCRIPTOR = "Lapp/revanced/integrations/patches/SpoofSignatureVerificationPatch;"
    }
}
