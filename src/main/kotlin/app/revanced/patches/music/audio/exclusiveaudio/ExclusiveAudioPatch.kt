package app.revanced.patches.music.audio.exclusiveaudio

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.audio.exclusiveaudio.fingerprints.AllowExclusiveAudioPlaybackFingerprint

@Patch(
    name = "Exclusive audio playback",
    description = "Enables the option to play audio without video.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")]
)
@Suppress("unused")
object ExclusiveAudioPatch : BytecodePatch(
    setOf(AllowExclusiveAudioPlaybackFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        AllowExclusiveAudioPlaybackFingerprint.result?.mutableMethod?.apply {
            addInstructions(
                0,
            """
                const/4 v0, 0x1
                return v0
            """
            )
        } ?: throw AllowExclusiveAudioPlaybackFingerprint.exception
    }
}