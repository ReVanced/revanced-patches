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
        val concatSignaturesIndex = getAppSignatureFingerprint.stringMatches!!.first().index - 2
        val register =
            getAppSignatureFingerprint.method.getInstruction<OneRegisterInstruction>(concatSignaturesIndex).registerA

        val expectedSignature = "d6a6dced4a85f24204bf9505ccc1fce114cadb32"

        getAppSignatureFingerprint.method.replaceInstruction(
            concatSignaturesIndex,
            "const-string v$register, \"$expectedSignature\"",
        )
    }
}
