package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val spoofPackageInfoPatch = bytecodePatch(
    name = "Spoof package info",
    description = "Spoofs the package info of the app to fix various functions of the app.",
) {
    compatibleWith("com.spotify.music")

    execute {
        getPackageInfoFingerprint.method.apply {
            // region Spoof signature.

            val failedToGetSignaturesStringIndex = getPackageInfoFingerprint.stringMatches!!.first().index

            val concatSignaturesIndex = indexOfFirstInstructionReversedOrThrow(
                failedToGetSignaturesStringIndex,
                Opcode.MOVE_RESULT_OBJECT,
            )

            val signatureRegister = getInstruction<OneRegisterInstruction>(concatSignaturesIndex).registerA
            val expectedSignature = "d6a6dced4a85f24204bf9505ccc1fce114cadb32"

            replaceInstruction(concatSignaturesIndex, "const-string v$signatureRegister, \"$expectedSignature\"")

            // endregion

            // region Spoof installer name.

            // Old versions of Spotify do not check the installer name.
            fingerprint { strings("Failed to get installer package") }.matchOrNull(this) ?: return@execute

            val returnInstallerNameIndex = indexOfFirstInstructionReversedOrThrow(
                instructions.size,
                Opcode.RETURN_OBJECT
            )

            val installerNameRegister = getInstruction<OneRegisterInstruction>(returnInstallerNameIndex).registerA
            val expectedInstallerName = "com.android.vending"

            addInstructionsAtControlFlowLabel(
                returnInstallerNameIndex,
                "const-string v$installerNameRegister, \"$expectedInstallerName\"",
            )

            // endregion
        }
    }
}