package app.revanced.patches.reddit.customclients.boostforreddit.fix.downloads

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val fixAudioMissingInDownloadsPatch = bytecodePatch(
    name = "Fix missing audio in video downloads",
    description = "Fixes audio missing in videos downloaded from v.redd.it.",
) {
    compatibleWith("com.rubenmayayo.reddit")

    execute {
        val endpointReplacements = mapOf(
            "/DASH_audio.mp4" to "/CMAF_AUDIO_128.mp4",
            "/audio" to "/CMAF_AUDIO_64.mp4",
        )

        downloadAudioFingerprint.method.apply {
            downloadAudioFingerprint.stringMatches!!.forEach { match ->
                val replacement = endpointReplacements[match.string]
                val register = getInstruction<OneRegisterInstruction>(match.index).registerA

                replaceInstruction(match.index, "const-string v$register, \"$replacement\"")
            }
        }
    }
}
