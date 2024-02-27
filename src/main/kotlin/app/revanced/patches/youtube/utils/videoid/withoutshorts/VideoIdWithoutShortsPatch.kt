package app.revanced.patches.youtube.utils.videoid.withoutshorts

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.utils.integrations.Constants.VIDEO_PATH
import app.revanced.patches.youtube.utils.videoid.withoutshorts.fingerprint.VideoIdWithoutShortsFingerprint
import app.revanced.util.exception
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

object VideoIdWithoutShortsPatch : BytecodePatch(
    setOf(VideoIdWithoutShortsFingerprint)
) {
    override fun execute(context: BytecodeContext) {

        VideoIdWithoutShortsFingerprint.result?.let {
            it.mutableMethod.apply {
                insertMethod = this
                insertIndex = implementation!!.instructions.indexOfFirst { instruction ->
                    instruction.opcode == Opcode.INVOKE_INTERFACE
                }
                insertRegister = getInstruction<OneRegisterInstruction>(insertIndex + 1).registerA
            }
        } ?: throw VideoIdWithoutShortsFingerprint.exception

        injectCall("$VIDEO_PATH/VideoInformation;->setVideoId(Ljava/lang/String;)V")

    }

    private var offset = 2

    private var insertIndex: Int = 0
    private var insertRegister: Int = 0
    private lateinit var insertMethod: MutableMethod


    /**
     * Adds an invoke-static instruction, called with the new id when the video changes
     * @param methodDescriptor which method to call. Params have to be `Ljava/lang/String;`
     */
    internal fun injectCall(
        methodDescriptor: String
    ) {
        insertMethod.addInstructions(
            insertIndex + offset, // move-result-object offset
            "invoke-static {v$insertRegister}, $methodDescriptor"
        )
    }
}

