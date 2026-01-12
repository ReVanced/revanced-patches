package app.revanced.patches.reddit.customclients.boostforreddit.fix.downloads

import app.revanced.patcher.extensions.getInstruction
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.util.findInstructionIndicesReversed
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused", "ObjectPropertyName")
val `Fix missing audio in video downloads` by creatingBytecodePatch(
    description = "Fixes audio missing in videos downloaded from v.redd.it.",
) {
    compatibleWith("com.rubenmayayo.reddit")

    apply {
        val endpointReplacements = mapOf(
            "/DASH_audio.mp4" to "/DASH_AUDIO_128.mp4",
            "/audio" to "/DASH_AUDIO_64.mp4",
        )

        downloadAudioMethod.apply {
            endpointReplacements.forEach { (target, replacement) ->
                // Find all occurrences of the target string in the method
                findInstructionIndicesReversed {
                    opcode == Opcode.CONST_STRING && getReference<StringReference>()?.string == target
                }.forEach { index ->
                    val register = getInstruction<OneRegisterInstruction>(index).registerA
                    replaceInstruction(index, "const-string v$register, \"$replacement\"")
                }
            }
        }
    }
}
