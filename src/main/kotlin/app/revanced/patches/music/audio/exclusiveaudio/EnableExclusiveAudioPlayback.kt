package app.revanced.patches.music.audio.exclusiveaudio

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.music.audio.exclusiveaudio.fingerprints.AllowExclusiveAudioPlaybackFingerprint
import app.revanced.util.exception

@Patch(
    name = "Enable exclusive audio playback",
    description = "Enables the option to play audio without video.",
    compatiblePackages = [CompatiblePackage("com.google.android.apps.youtube.music")],
)
@Suppress("unused")
object EnableExclusiveAudioPlayback : BytecodePatch(
    setOf(AllowExclusiveAudioPlaybackFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        AllowExclusiveAudioPlaybackFingerprint.result?.mutableMethod?.apply {
            addInstructions(
                0,
                """
                const/4 v0, 0x1
                return v0
            """,
            )
        } ?: throw AllowExclusiveAudioPlaybackFingerprint.exception
    }
}

@Deprecated("This patch class has been renamed to EnableExclusiveAudioPlayback.")
object ExclusiveAudioPatch : BytecodePatch(emptySet()) {
    override fun execute(context: BytecodeContext) = EnableExclusiveAudioPlayback.execute(context)
}
