package app.revanced.patches.reddit.customclients.boostforreddit.fix.downloads

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val fixMissingAudioInVideoDownloadsPatch = bytecodePatch(
    name = "Fix missing audio in video downloads",
    description = "Fixes audio missing in videos downloaded from v.redd.it.",
) {
    compatibleWith("com.rubenmayayo.reddit")

    apply {
        val endpointReplacements = arrayOf(
            "/DASH_AUDIO_128.mp4",
            "/DASH_AUDIO_64.mp4",
        )

        downloadAudioMethodMatch.indices[0].forEachIndexed { index, i ->
            val replacement = endpointReplacements[i]
            val register = downloadAudioMethodMatch.method.getInstruction<OneRegisterInstruction>(index).registerA

            downloadAudioMethodMatch.method.replaceInstruction(index, "const-string v$register, \"$replacement\"")
        }
    }
}
