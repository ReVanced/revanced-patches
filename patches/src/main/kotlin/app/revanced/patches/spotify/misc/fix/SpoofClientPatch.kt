package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.shared.misc.hex.Replacement
import app.revanced.patches.shared.misc.hex.hexPatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal const val EXTENSION_CLASS_DESCRIPTOR =  "Lapp/revanced/extension/spotify/misc/fix/SpoofClientPatch;"
internal const val LOGIN_SERVER_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/misc/fix/LoginServer;"

internal fun makeUrlPatch(targetFilePath: String): Sequence<Replacement> {
    return setOf(
        Replacement(
            "68 74 74 70 73 3A 2F 2F 6C 6F 67 69 6E 35 2E 73 70 6F 74 69 66 79 2E 63 6F 6D 2F 76 33 2F 6C 6F 67 69 6E",
            // http://127.0.0.1:4345/v3/login
            "68 74 74 70 3A 2F 2F 31 32 37 2E 30 2E 30 2E 31 3A 34 33 34 35 2F 76 33 2F 6C 6F 67 69 6E 00 00 00 00 00",
            targetFilePath,
        ),
        Replacement(
            "68 74 74 70 73 3A 2F 2F 6C 6F 67 69 6E 35 2E 73 70 6F 74 69 66 79 2E 63 6F 6D 2F 76 34 2F 6C 6F 67 69 6E",
            // http://127.0.0.1:4345/v4/login
            "68 74 74 70 3A 2F 2F 31 32 37 2E 30 2E 30 2E 31 3A 34 33 34 35 2F 76 34 2F 6C 6F 67 69 6E 00 00 00 00 00",
            targetFilePath,
        )
    ).asSequence()
}

internal val spoofClientPatchHexPatch = hexPatch(false) {
    val replacements = makeUrlPatch("lib/arm64-v8a/liborbit-jni-spotify.so") +
            makeUrlPatch("lib/armeabi-v7a/liborbit-jni-spotify.so") +
            makeUrlPatch("lib/x86/liborbit-jni-spotify.so") +
            makeUrlPatch("lib/x86_64/liborbit-jni-spotify.so")

    replacements.toSet()
}

@Suppress("unused")
val spoofClientPatch = bytecodePatch(
    name = "Spoof client",
    description = "Spoofs the client to fix various functions of the app.",
) {
    dependsOn(
        sharedExtensionPatch,
        spoofClientPatchHexPatch
    )

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

        startLiborbitFingerprint.method.addInstructions(
            0,
            """
                # Port 4345
                const/16 v0, 0x10F9
                invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->startLoginServer(I)V
            """
        )

        startupPageLayoutInflateFingerprint.method.apply {
            val openLoginWebViewDescriptor =
                "$LOGIN_SERVER_CLASS_DESCRIPTOR->openLoginWebView(Landroid/view/LayoutInflater;)V"

            addInstructions(
                0,
                """
                    move-object/from16 v3, p1
                    invoke-static { v3 }, $openLoginWebViewDescriptor
                """
            )
        }
    }
}
