package app.revanced.patches.youtube.misc.videoid.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.videoid.annotation.VideoIdCompatibility
import app.revanced.patches.youtube.misc.videoid.fingerprint.VideoIdFingerprint
import org.jf.dexlib2.iface.instruction.formats.Instruction11x

@Name("video-id-hook")
@Description("hook to detect when the video id changes")
@VideoIdCompatibility
@Version("0.0.1")
@Dependencies([IntegrationsPatch::class])
class VideoIdPatch : BytecodePatch(
    listOf(
        VideoIdFingerprint
    )
) {
    override fun execute(data: BytecodeData): PatchResult {
        injectCall("Lapp/revanced/integrations/videoplayer/VideoInformation;->setCurrentVideoId(Ljava/lang/String;)V")

        return PatchResultSuccess()
    }

    companion object {
        private var offset = 2

        /**
         * Adds an invoke-static instruction, called with the new id when the video changes
         * @param methodDescriptor which method to call. Params have to be `Ljava/lang/String;`
         */
        fun injectCall(
            methodDescriptor: String
        ) {
            val result = VideoIdFingerprint.result!!

            val method = result.mutableMethod
            val videoIdRegister =
                (method.implementation!!.instructions[result.patternScanResult!!.endIndex + 1] as Instruction11x).registerA
            method.addInstructions(
                result.patternScanResult!!.endIndex + offset, // after the move-result-object
                "invoke-static {v$videoIdRegister}, $methodDescriptor"
            )
            offset++ // so additional instructions get added later
        }
    }
}

