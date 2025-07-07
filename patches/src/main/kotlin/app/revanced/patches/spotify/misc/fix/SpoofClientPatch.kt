package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.intOption
import app.revanced.patches.shared.misc.hex.HexPatchBuilder
import app.revanced.patches.shared.misc.hex.hexPatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.*
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/misc/fix/SpoofClientPatch;"

@Suppress("unused")
val spoofClientPatch = bytecodePatch(
    name = "Spoof client",
    description = "Spoofs the client to fix various functions of the app.",
) {
    val requestListenerPort by intOption(
        key = "requestListenerPort",
        default = 4345,
        title = " Login request listener port",
        description = "The port to use for the listener that intercepts and handles login requests. " +
                "Port must be between 0 and 65535.",
        required = true,
        validator = {
            it!!
            !(it < 0 || it > 65535)
        }
    )

    dependsOn(
        sharedExtensionPatch,
    )

    compatibleWith("com.spotify.music")

    execute {
        // region Spoof package info.

        getPackageInfoFingerprint.method.apply {
            // region Spoof signature.

            val failedToGetSignaturesStringIndex =
                getPackageInfoFingerprint.stringMatches!!.first().index

            val concatSignaturesIndex = indexOfFirstInstructionReversedOrThrow(
                failedToGetSignaturesStringIndex,
                Opcode.MOVE_RESULT_OBJECT,
            )

            val signatureRegister = getInstruction<OneRegisterInstruction>(concatSignaturesIndex).registerA
            val expectedSignature = "d6a6dced4a85f24204bf9505ccc1fce114cadb32"

            replaceInstruction(concatSignaturesIndex, "const-string v$signatureRegister, \"$expectedSignature\"")

            // endregion

            // region Spoof installer name.

            val expectedInstallerName = "com.android.vending"

            findInstructionIndicesReversedOrThrow {
                val reference = getReference<MethodReference>()
                reference?.name == "getInstallerPackageName" || reference?.name == "getInstallingPackageName"
            }.forEach { index ->
                val returnObjectIndex = index + 1

                val installerPackageNameRegister = getInstruction<OneRegisterInstruction>(
                    returnObjectIndex
                ).registerA

                addInstruction(
                    returnObjectIndex + 1,
                    "const-string v$installerPackageNameRegister, \"$expectedInstallerName\""
                )
            }

            // endregion
        }

        // endregion

        // region Spoof client.

        // endregion

        // region Disable verdicts.

        // Early return to block sending bad verdicts to the API.
        runIntegrityVerificationFingerprint.method.returnEarly()

        // endregion
    }
}
