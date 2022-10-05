package app.revanced.patches.youtube.misc.videoid.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprintResult
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.videoid.annotation.VideoIdCompatibility
import app.revanced.patches.youtube.misc.videoid.fingerprint.VideoIdFingerprint
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Name("video-id-hook")
@Description("Hook to detect when the video id changes")
@VideoIdCompatibility
@Version("0.0.1")
@DependsOn([IntegrationsPatch::class])
class VideoIdPatch : BytecodePatch(
    listOf(
        VideoIdFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        result = VideoIdFingerprint.result!!

        insertMethod = result.mutableMethod
        videoIdRegister =
            (insertMethod.implementation!!.instructions[result.scanResult.patternScanResult!!.endIndex + 1] as OneRegisterInstruction).registerA

        injectCall("Lapp/revanced/integrations/videoplayer/VideoInformation;->setCurrentVideoId(Ljava/lang/String;)V")

        offset++ // offset so setCurrentVideoId is called before any injected call

        return PatchResultSuccess()
    }

    companion object {
        private var offset = 2

        private var videoIdRegister: Int = 0
        private lateinit var result: MethodFingerprintResult
        private lateinit var insertMethod: MutableMethod

        /**
         * Adds an invoke-static instruction, called with the new id when the video changes
         * @param methodDescriptor which method to call. Params have to be `Ljava/lang/String;`
         */
        fun injectCall(
            methodDescriptor: String
        ) {
            insertMethod.addInstructions(
                result.scanResult.patternScanResult!!.endIndex + offset, // move-result-object offset
                "invoke-static {v$videoIdRegister}, $methodDescriptor"
            )
        }
    }
}

