package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val spoofSignaturePatch = bytecodePatch(
    name = "Spoof signature",
    description = "Spoofs the signature of the app to fix various functions of the app.",
) {
    compatibleWith("com.spotify.music")

    execute {
        getAppSignatureFingerprint.let {
            it.method.apply {
                val index = it.instructionMatches[1].index
                val register = getInstruction<OneRegisterInstruction>(index).registerA
                val expectedSignature = "d6a6dced4a85f24204bf9505ccc1fce114cadb32"

                replaceInstruction(index, "const-string v$register, \"$expectedSignature\"")
            }
        }
    }
}
