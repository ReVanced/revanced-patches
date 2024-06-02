package app.revanced.patches.reddit.customclients.boostforreddit.fix.downloadVReddItAudio

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.reddit.customclients.boostforreddit.fix.downloadVReddItAudio.fingerprints.DownloaderSelectVReddItAudioLink
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference

@Patch(
    name = "Fix downloads from v.redd.it",
    description = "Fixes missing audio in videos downloaded from v.redd.it",
    compatiblePackages = [CompatiblePackage("com.rubenmayayo.reddit")]
)
object FixDownloadedVReddItAudio : BytecodePatch(setOf(DownloaderSelectVReddItAudioLink)) {
    private val INDEXES = setOf(
        Pair(2, "/DASH_AUDIO_128.mp4"),
        Pair(3, "/DASH_AUDIO_64.mp4")
    )

    override fun execute(context: BytecodeContext) {
        val searchResult = DownloaderSelectVReddItAudioLink.result
        searchResult?.mutableMethod?.apply {
            INDEXES.forEach { (stringIndex, endpoint) ->
                val instructionIndex = searchResult.scanResult.stringsScanResult!!.matches[stringIndex].index
                val instruction = getInstruction(instructionIndex) as Instruction21c
                replaceInstruction(
                    instructionIndex,
                    BuilderInstruction21c(
                        instruction.opcode,
                        instruction.registerA,
                        ImmutableStringReference(endpoint)
                    )
                )
            }
        }
    }
}