package app.revanced.patches.reddit.customclients.boostforreddit.fix.downloads

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.boostforreddit.fix.downloads.fingerprints.DownloadAudioFingerprint
import app.revanced.util.resultOrThrow
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Patch(
    name = "Fix missing audio in video downloads",
    description = "Fixes audio missing in videos downloaded from v.redd.it.",
    compatiblePackages = [CompatiblePackage("com.rubenmayayo.reddit")],
)
@Suppress("unused")
object FixAudioMissingInDownloadsPatch : BytecodePatch(
    setOf(DownloadAudioFingerprint),
) {
    private val endpointReplacements = mapOf(
        "/DASH_audio.mp4" to "/DASH_AUDIO_128.mp4",
        "/audio" to "/DASH_AUDIO_64.mp4",
    )
    override fun execute(context: BytecodeContext) {
        DownloadAudioFingerprint.resultOrThrow().let { result ->
            result.scanResult.stringsScanResult!!.matches.take(2).forEach { match ->
                result.mutableMethod.apply {
                    val replacement = endpointReplacements[match.string]
                    val register = getInstruction<OneRegisterInstruction>(match.index).registerA

                    replaceInstruction(match.index, "const-string v$register, \"$replacement\"")
                }
            }
        }
    }
}
