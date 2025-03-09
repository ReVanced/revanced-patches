package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val spoofSignaturePatch = bytecodePatch(
    name = "Spoof signature",
    description = "Spoofs the signature of the app to fix various functions of the app.",
) {
    compatibleWith("com.spotify.music")

    execute {
        getAppSignatureFingerprint.method.apply {
            val failedToGetSignaturesStringMatch = getAppSignatureFingerprint.stringMatches!!.first()

            val concatSignaturesIndex = indexOfFirstInstructionReversedOrThrow(
                failedToGetSignaturesStringMatch.index,
                Opcode.MOVE_RESULT_OBJECT,
            )

            val register = getInstruction<OneRegisterInstruction>(concatSignaturesIndex).registerA

            val expectedSignature = "d6a6dced4a85f24204bf9505ccc1fce114cadb32"

            replaceInstruction(concatSignaturesIndex, "const-string v$register, \"$expectedSignature\"")
        }
    }
}
