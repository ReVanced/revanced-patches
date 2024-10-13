package app.revanced.patches.youtube.video.videoid

import app.revanced.patcher.Match
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod
import app.revanced.patches.youtube.misc.extension.sharedExtensionPatch
import app.revanced.patches.youtube.misc.playertype.playerTypeHookPatch
import app.revanced.patches.youtube.video.playerresponse.Hook
import app.revanced.patches.youtube.video.playerresponse.addPlayerResponseMethodHook
import app.revanced.patches.youtube.video.playerresponse.playerResponseMethodHookPatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

/**
 * Hooks the new video id when the video changes.
 *
 * Supports all videos (regular videos and Shorts).
 *
 * _Does not function if playing in the background with no video visible_.
 *
 * Be aware, this can be called multiple times for the same video id.
 *
 * @param methodDescriptor which method to call. Params have to be `Ljava/lang/String;`
 */
fun hookVideoId(
    methodDescriptor: String,
) = videoIdMethod.addInstruction(
    videoIdInsertIndex++,
    "invoke-static {v$videoIdRegister}, $methodDescriptor",
)

/**
 * Alternate hook that supports only regular videos, but hook supports changing to new video
 * during background play when no video is visible.
 *
 * _Does not support Shorts_.
 *
 * Be aware, the hook can be called multiple times for the same video id.
 *
 * @param methodDescriptor which method to call. Params have to be `Ljava/lang/String;`
 */
fun hookBackgroundPlayVideoId(
    methodDescriptor: String,
) = backgroundPlaybackMethod.addInstruction(
    backgroundPlaybackInsertIndex++, // move-result-object offset
    "invoke-static {v$backgroundPlaybackVideoIdRegister}, $methodDescriptor",
)

/**
 * Hooks the video id of every video when loaded.
 * Supports all videos and functions in all situations.
 *
 * First parameter is the video id.
 * Second parameter is if the video is a Short AND it is being opened or is currently playing.
 *
 * Hook is always called off the main thread.
 *
 * This hook is called as soon as the player response is parsed,
 * and called before many other hooks are updated such as [playerTypeHookPatch].
 *
 * Note: The video id returned here may not be the current video that's being played.
 * It's common for multiple Shorts to load at once in preparation
 * for the user swiping to the next Short.
 *
 * For most use cases, you probably want to use
 * [hookVideoId] or [hookBackgroundPlayVideoId] instead.
 *
 * Be aware, this can be called multiple times for the same video id.
 *
 * @param methodDescriptor which method to call. Params must be `Ljava/lang/String;Z`
 */
fun hookPlayerResponseVideoId(methodDescriptor: String) = addPlayerResponseMethodHook(
    Hook.VideoId(
        methodDescriptor,
    ),
)

private var videoIdRegister = 0
private var videoIdInsertIndex = 0
private lateinit var videoIdMethod: MutableMethod

private var backgroundPlaybackVideoIdRegister = 0
private var backgroundPlaybackInsertIndex = 0
private lateinit var backgroundPlaybackMethod: MutableMethod

val videoIdPatch = bytecodePatch(
    description = "Hooks to detect when the video id changes.",
) {
    dependsOn(
        sharedExtensionPatch,
        playerResponseMethodHookPatch,
    )

    val videoIdMatch by videoIdFingerprint()
    val videoIdBackgroundPlayMatch by videoIdBackgroundPlayFingerprint()

    execute {
        /**
         * Supplies the method and register index of the video id register.
         *
         * @param consumer Consumer that receives the method, insert index and video id register index.
         */
        fun Match.setFields(consumer: (MutableMethod, Int, Int) -> Unit) {
            val videoIdRegisterIndex = patternMatch!!.endIndex

            mutableMethod.let {
                val videoIdRegister = it.getInstruction<OneRegisterInstruction>(videoIdRegisterIndex).registerA
                val insertIndex = videoIdRegisterIndex + 1
                consumer(it, insertIndex, videoIdRegister)
            }
        }

        videoIdMatch.setFields { method, index, register ->
            videoIdMethod = method
            videoIdInsertIndex = index
            videoIdRegister = register
        }

        videoIdBackgroundPlayMatch.setFields { method, insertIndex, videoIdRegister ->
            backgroundPlaybackMethod = method
            backgroundPlaybackInsertIndex = insertIndex
            backgroundPlaybackVideoIdRegister = videoIdRegister
        }
    }
}
