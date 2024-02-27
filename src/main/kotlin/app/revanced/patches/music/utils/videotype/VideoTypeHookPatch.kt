package app.revanced.patches.music.utils.videotype

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.music.utils.integrations.Constants.UTILS_PATH
import app.revanced.patches.music.utils.videotype.fingerprint.VideoTypeFingerprint
import app.revanced.patches.music.utils.videotype.fingerprint.VideoTypeParentFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Suppress("unused")
object VideoTypeHookPatch : BytecodePatch(
    setOf(VideoTypeParentFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        VideoTypeParentFingerprint.result?.let { parentResult ->
            VideoTypeFingerprint.also {
                it.resolve(
                    context,
                    parentResult.classDef
                )
            }.result?.let {
                it.mutableMethod.apply {
                    val insertIndex = it.scanResult.patternScanResult!!.startIndex + 3
                    val referenceIndex = insertIndex + 1
                    val referenceInstruction =
                        getInstruction<ReferenceInstruction>(referenceIndex).reference

                    addInstructionsWithLabels(
                        insertIndex, """
                            if-nez p0, :dismiss
                            sget-object p0, $referenceInstruction
                            :dismiss
                            invoke-static {p0}, $INTEGRATIONS_CLASS_DESCRIPTOR->setVideoType(Ljava/lang/Enum;)V
                            """
                    )
                }
            } ?: throw VideoTypeFingerprint.exception
        } ?: throw VideoTypeParentFingerprint.exception
    }

    private const val INTEGRATIONS_CLASS_DESCRIPTOR =
        "$UTILS_PATH/VideoTypeHookPatch;"
}
