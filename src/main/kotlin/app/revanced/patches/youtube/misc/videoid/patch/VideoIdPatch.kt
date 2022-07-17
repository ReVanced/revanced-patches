package app.revanced.patches.youtube.misc.videoid.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.impl.BytecodeData
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Dependencies
import app.revanced.patcher.patch.impl.BytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.videoid.annotation.VideoIdCompatibility
import app.revanced.patches.youtube.misc.videoid.fingerprint.VideoIdFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

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
        result = VideoIdFingerprint.result!!

        insertMethod = result.mutableMethod
        videoIdRegister =
            (insertMethod.implementation!!.instructions[result.patternScanResult!!.endIndex + 1] as OneRegisterInstruction).registerA

        injectCall("Lapp/revanced/integrations/videoplayer/VideoInformation;->setCurrentVideoId(Ljava/lang/String;)V")

        return PatchResultSuccess()
    }

    companion object {
        private lateinit var result: MethodFingerprintResult
        private var videoIdRegister: Int = 0
        private lateinit var insertMethod: MutableMethod
        private var offset = 2

        /**
         * Adds an invoke-static instruction, called with the new id when the video changes
         * @param methodDescriptor which method to call. Params have to be `Ljava/lang/String;`
         */
        fun injectCall(
            methodDescriptor: String
        ) {
            insertMethod.addInstructions(
                result.patternScanResult!!.endIndex + offset, // after the move-result-object
                "invoke-static {v$videoIdRegister}, $methodDescriptor"
            )
            offset++ // so additional instructions get added later
        }
    }
}

