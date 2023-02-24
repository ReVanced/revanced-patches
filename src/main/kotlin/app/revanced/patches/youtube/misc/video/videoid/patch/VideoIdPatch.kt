package app.revanced.patches.youtube.misc.video.videoid.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.video.videoid.annotation.VideoIdCompatibility
import app.revanced.patches.youtube.misc.video.videoid.fingerprint.VideoIdFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Name("video-id-hook")
@Description("Hooks to detect when the video id changes")
@VideoIdCompatibility
@Version("0.0.1")
@DependsOn([IntegrationsPatch::class])
class VideoIdPatch : BytecodePatch(
    listOf(VideoIdFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        VideoIdFingerprint.result?.let {
            val videoIdRegisterInstructionIndex = it.scanResult.patternScanResult!!.endIndex

            with(it.mutableMethod) {
                insertMethod = this
                videoIdRegister = (instruction(videoIdRegisterInstructionIndex) as OneRegisterInstruction).registerA
                insertIndex = videoIdRegisterInstructionIndex + 1
            }
        } ?: return VideoIdFingerprint.toErrorResult()

        return PatchResultSuccess()
    }

    companion object {
        private var videoIdRegister = 0
        private var insertIndex = 0

        private lateinit var insertMethod: MutableMethod

        /**
         * Adds an invoke-static instruction, called with the new id when the video changes
         * @param methodDescriptor which method to call. Params have to be `Ljava/lang/String;`
         */
        fun injectCall(
            methodDescriptor: String
        ) {
            insertMethod.addInstructions(
                // TODO: The order has been proven to not be required, so remove the logic for keeping order.
                // Keep injection calls in the order they're added:
                // Increment index. So if additional injection calls are added, those calls run after this injection call.
                insertIndex++,
                "invoke-static {v$videoIdRegister}, $methodDescriptor"
            )
        }
    }
}

