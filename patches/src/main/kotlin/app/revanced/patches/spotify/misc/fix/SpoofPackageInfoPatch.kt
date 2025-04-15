package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.extension.IS_SPOTIFY_LEGACY_APP_TARGET
import app.revanced.util.addInstructionsAtControlFlowLabel
import app.revanced.util.indexOfFirstInstructionOrThrow
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
        val getPackageInfoFingerprint = if (IS_SPOTIFY_LEGACY_APP_TARGET) {
            getPackageInfoLegacyFingerprint
        } else {
            getPackageInfoFingerprint
        }

        getPackageInfoFingerprint.method.apply {
            val stringMatches = getPackageInfoFingerprint.stringMatches!!

            // region Spoof signature.

            val failedToGetSignaturesStringIndex = stringMatches.first().index

            val concatSignaturesIndex = indexOfFirstInstructionReversedOrThrow(
                failedToGetSignaturesStringIndex,
                Opcode.MOVE_RESULT_OBJECT,
            )

            val signatureRegister = getInstruction<OneRegisterInstruction>(concatSignaturesIndex).registerA
            val expectedSignature = "d6a6dced4a85f24204bf9505ccc1fce114cadb32"

            replaceInstruction(concatSignaturesIndex, "const-string v$signatureRegister, \"$expectedSignature\"")

            // endregion

            // region Spoof installer name.

            if (IS_SPOTIFY_LEGACY_APP_TARGET) {
                // Installer name is not used in the legacy app target.
                return@execute
            }

            val expectedInstallerName = "com.android.vending"

            val returnInstallerNameIndex = indexOfFirstInstructionOrThrow(
                stringMatches.last().index,
                Opcode.RETURN_OBJECT
            )

            val installerNameRegister = getInstruction<OneRegisterInstruction>(
                returnInstallerNameIndex
            ).registerA

            addInstructionsAtControlFlowLabel(
                returnInstallerNameIndex,
                "const-string v$installerNameRegister, \"$expectedInstallerName\""
            )

            // endregion
        }
    }
}