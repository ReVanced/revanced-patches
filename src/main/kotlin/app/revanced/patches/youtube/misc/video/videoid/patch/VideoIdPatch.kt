package app.revanced.patches.youtube.misc.video.videoid.patch

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
    listOf(
        VideoIdFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        with(VideoIdFingerprint.result!!) {
            insertMethod = mutableMethod
            insertIndex = scanResult.patternScanResult!!.endIndex + 2

            videoIdRegister = (insertMethod.instruction(insertIndex - 1) as OneRegisterInstruction).registerA
        }

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
        ) = insertMethod.addInstructions(
            insertIndex, // move-result-object offset
            "invoke-static {v$videoIdRegister}, $methodDescriptor"
        )
    }
}

