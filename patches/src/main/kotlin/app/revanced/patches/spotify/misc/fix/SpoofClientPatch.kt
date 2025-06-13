package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/misc/fix/SpoofClientPatch;"

@Suppress("unused")
val spoofClientPatch = bytecodePatch(
    name = "Spoof client",
    description = "Spoofs the client to fix various functions of the app.",
) {
    dependsOn(sharedExtensionPatch)

    compatibleWith("com.spotify.music")

    execute {
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

        getAuthenticateResultFingerprint.apply {
            val parseFailedStringIndex = getAuthenticateResultFingerprint.stringMatches!!.first().index

            getAuthenticateResultFingerprint.method.apply {
                val returnIndex = indexOfFirstInstructionReversedOrThrow(parseFailedStringIndex, Opcode.CHECK_CAST) + 3
                val returnRegister = getInstruction<OneRegisterInstruction>(returnIndex).registerA

                // TODO: Obtain references to client and access token here.

                val transferSessionMethod = "$EXTENSION_CLASS_DESCRIPTOR->" +
                        "transferSession(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"

                addInstruction(
                    returnIndex,
                    """
                       invoke-static { v0, v1 }, $transferSessionMethod
                       move-result-object v$returnRegister
                       # TODO: Replace the token with the new one.
                    """
                )
            }

        }
    }
}
