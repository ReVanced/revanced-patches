package app.revanced.patches.reddit.customclients.boostforreddit.fix.downloadVReddItAudio

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.boostforreddit.fix.downloadVReddItAudio.fingerprints.DownloaderSelectVReddItAudioLink

@Patch(
    name="Fix audio in videos downloaded from v.redd.it",
    compatiblePackages = [CompatiblePackage("com.rubenmayayo.reddit")]
)
object FixDownloadedVReddItAudio : BytecodePatch(setOf(DownloaderSelectVReddItAudioLink)) {
    override fun execute(context: BytecodeContext) {
        DownloaderSelectVReddItAudioLink.result?.mutableMethod?.replaceInstructions(17, "const-string v3, \"/DASH_AUDIO_128.mp4\"")
        DownloaderSelectVReddItAudioLink.result?.mutableMethod?.replaceInstructions(34, "const-string p2, \"/DASH_AUDIO_64.mp4\"")
    }
}