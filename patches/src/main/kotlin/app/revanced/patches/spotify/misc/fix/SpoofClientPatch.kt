package app.revanced.patches.spotify.misc.fix

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.intOption
import app.revanced.patches.shared.misc.hex.Replacement
import app.revanced.patches.shared.misc.hex.hexPatch
import app.revanced.patches.spotify.misc.extension.sharedExtensionPatch
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/spotify/misc/fix/SpoofClientPatch;"
internal const val EXTENSION_CLASS_HELPER = "Lapp/revanced/extension/spotify/misc/fix/Helper;"

@Suppress("unused")
val spoofClientPatch = bytecodePatch(
    name = "Spoof client",
    description = "Spoofs the client to fix various functions of the app.",
) {
    val port by intOption(
        key = "port",
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
        hexPatch(false) {
            listOf(
                "arm64-v8a",
                "armeabi-v7a",
                "x86",
                "x86_64"
            ).map {
                listOf(
                    // Replace https://login5.spotify.com/v3/login with http://127.0.0.1:4345/v3/login.
                    Replacement(
                        "68 74 74 70 73 3A 2F 2F " +
                                "6C 6F 67 69 6E 35 2E 73 " +
                                "70 6F 74 69 66 79 2E 63 " +
                                "6F 6D 2F 76 33 2F 6C 6F " +
                                "67 69 6E",
                        "68 74 74 70 3A 2F 2F 31 " +
                                "32 37 2E 30 2E 30 2E 31 " +
                                "3A 34 33 34 35 2F 76 33 " +
                                "2F 6C 6F 67 69 6E 00 00 " +
                                "00 00 00",
                        "lib/$it/liborbit-jni-spotify.so",
                    ),
                    // Replace https://login5.spotify.com/v4/login with http://127.0.0.1:4345/v4/login.
                    Replacement(
                        "68 74 74 70 73 3A 2F 2F " +
                                "6C 6F 67 69 6E 35 2E 73 " +
                                "70 6F 74 69 66 79 2E 63 " +
                                "6F 6D 2F 76 34 2F 6C 6F " +
                                "67 69 6E",
                        "68 74 74 70 3A 2F 2F 31 " +
                                "32 37 2E 30 2E 30 2E 31 " +
                                "3A 34 33 34 35 2F 76 34 " +
                                "2F 6C 6F 67 69 6E 00 00 " +
                                "00 00 00",
                        "lib/$it/liborbit-jni-spotify.so",
                    )
                )
            }.flatten().toSet()
        }
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
                const/16 v0, $port
                invoke-static { v0 }, $EXTENSION_CLASS_DESCRIPTOR->listen(I)V
            """
        )

        startupPageLayoutInflateFingerprint.method.apply {
            val openLoginWebViewDescriptor =
                "$EXTENSION_CLASS_DESCRIPTOR->login(Landroid/view/LayoutInflater;)V"

            addInstructions(
                0,
                """
                    move-object/from16 v3, p1
                    invoke-static { v3 }, $openLoginWebViewDescriptor
                """
            )
        }

        loginSetListenerFingerprint.method.apply {
            val returnIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.RETURN_VOID
            }

            addInstructions(
                returnIndex,
                """
                    iget-object p1, p0, Lp/grw;->E1:Landroid/widget/Button;

                    invoke-virtual {p1}, Landroid/view/View;->performClick()Z
                    """
            )
        }

        loginOnClickFingerprint.method.apply {
            val returnIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.RETURN_VOID
            }

            addInstructions(
                returnIndex - 1,
                """
                    const-string v2, "bogus"
                    const-string p1, "bogus"
                    """
            )
        }

        firstLoginScreenFingerprint.method.apply {
            val onEventIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_INTERFACE && getReference<MethodReference>()?.name == "getView"
            }

            addInstructions(
                onEventIndex + 2,
                """
                    invoke-static {v4}, $EXTENSION_CLASS_HELPER->setButton(Landroid/view/View;)V
                    """
            )

            val returnIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.RETURN_VOID
            }

            addInstructions(
                returnIndex,
                """
                    invoke-static {}, $EXTENSION_CLASS_HELPER->getButton()Landroid/view/View;
                    move-result-object v0
                    invoke-virtual {v0}, Landroid/view/View;->performClick()Z
                    """
            )
        }

    }
}
