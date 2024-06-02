package app.revanced.patches.reddit.customclients.boostforreddit.fix.downloadVReddItAudio

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.boostforreddit.fix.downloadVReddItAudio.fingerprints.DownloaderSelectVReddItAudioLink
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c

@Patch(
    name="Fix downloads from v.redd.it",
    description = "Fixes missing audio in videos downloaded from v.redd.it",
    compatiblePackages = [CompatiblePackage("com.rubenmayayo.reddit")]
)
object FixDownloadedVReddItAudio : BytecodePatch(setOf(DownloaderSelectVReddItAudioLink)) {
    private val INDEXES = setOf(
        Pair(17, "/DASH_AUDIO_128.mp4"),
        Pair(34, "/DASH_AUDIO_64.mp4")
    )
    override fun execute(context: BytecodeContext) {
        DownloaderSelectVReddItAudioLink.result?.mutableMethod?.apply {
            INDEXES.forEach {
                val instruction = getInstruction(it.first) as Instruction21c
                replaceInstruction(it.first, "const-string v${instruction.registerA}, \"${it.second}\"")
            }
        }
    }
}