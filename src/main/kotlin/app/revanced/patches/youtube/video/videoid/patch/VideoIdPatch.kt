package app.revanced.patches.youtube.video.videoid.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.video.videoid.annotation.VideoIdCompatibility
import app.revanced.patches.youtube.video.videoid.fingerprint.VideoIdFingerprint
import app.revanced.patches.youtube.video.videoid.fingerprint.VideoIdFingerprintBackgroundPlay
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Name("Video id hook")
@Description("Hooks to detect when the video id changes")
@VideoIdCompatibility
@DependsOn([IntegrationsPatch::class])
class VideoIdPatch : BytecodePatch(
    listOf(VideoIdFingerprint, VideoIdFingerprintBackgroundPlay)
) {
    override fun execute(context: BytecodeContext) {
        /**
         * Supplies the method and register index of the video id register.
         *
         * @param consumer Consumer that receives the method, insert index and video id register index.
         */
        fun MethodFingerprint.setFields(consumer: (MutableMethod, Int, Int) -> Unit) = result?.let { result ->
            val videoIdRegisterIndex = result.scanResult.patternScanResult!!.endIndex

            result.mutableMethod.let {
                val videoIdRegister = it.getInstruction<OneRegisterInstruction>(videoIdRegisterIndex).registerA
                val insertIndex = videoIdRegisterIndex + 1
                consumer(it, insertIndex, videoIdRegister)

            }
        } ?: throw VideoIdFingerprint.exception

        VideoIdFingerprint.setFields { method, insertIndex, videoIdRegister ->
            insertMethod = method
            VideoIdPatch.insertIndex = insertIndex
            VideoIdPatch.videoIdRegister = videoIdRegister
        }

        VideoIdFingerprintBackgroundPlay.setFields { method, insertIndex, videoIdRegister ->
            backgroundPlaybackMethod = method
            backgroundPlaybackInsertIndex = insertIndex
            backgroundPlaybackVideoIdRegister = videoIdRegister
        }
    }

    companion object {
        private var videoIdRegister = 0
        private var insertIndex = 0
        private lateinit var insertMethod: MutableMethod

        private var backgroundPlaybackVideoIdRegister = 0
        private var backgroundPlaybackInsertIndex = 0
        private lateinit var backgroundPlaybackMethod: MutableMethod

        /**
         * Adds an invoke-static instruction, called with the new id when the video changes.
         *
         * Supports all videos (regular videos, Shorts and Stories).
         *
         * _Does not function if playing in the background with no video visible_.
         *
         * Be aware, this can be called multiple times for the same video id.
         *
         * @param methodDescriptor which method to call. Params have to be `Ljava/lang/String;`
         */
        fun injectCall(
            methodDescriptor: String
        ) = insertMethod.addInstruction(
            // Keep injection calls in the order they're added:
            // Increment index. So if additional injection calls are added, those calls run after this injection call.
            insertIndex++,
            "invoke-static {v$videoIdRegister}, $methodDescriptor"
        )

        /**
         * Alternate hook that supports only regular videos, but hook supports changing to new video
         * during background play when no video is visible.
         *
         * _Does not support Shorts or Stories_.
         *
         * Be aware, the hook can be called multiple times for the same video id.
         *
         * @param methodDescriptor which method to call. Params have to be `Ljava/lang/String;`
         */
        fun injectCallBackgroundPlay(
            methodDescriptor: String
        ) = backgroundPlaybackMethod.addInstruction(
            backgroundPlaybackInsertIndex++, // move-result-object offset
                "invoke-static {v$backgroundPlaybackVideoIdRegister}, $methodDescriptor"
            )
    }
}

