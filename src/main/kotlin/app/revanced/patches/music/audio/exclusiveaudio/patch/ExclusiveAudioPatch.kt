package app.revanced.patches.music.audio.exclusiveaudio.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.music.annotations.MusicCompatibility
import app.revanced.patches.music.audio.exclusiveaudio.fingerprints.AllowExclusiveAudioPlaybackFingerprint

@Patch
@Name("Exclusive audio playback")
@Description("Enables the option to play audio without video.")
@MusicCompatibility
class ExclusiveAudioPatch : BytecodePatch(
    listOf(AllowExclusiveAudioPlaybackFingerprint)
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