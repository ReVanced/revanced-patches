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
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
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
        hexPatch(ignoreMissingTargetFiles = true, block = fun HexPatchBuilder.() {
            listOf(
                "arm64-v8a",
                "armeabi-v7a",
                "x86",
                "x86_64"
            ).forEach { architecture ->
                "https://login5.spotify.com/v3/login" to "http://127.0.0.1:$port/v3/login" inFile
                        "lib/$architecture/liborbit-jni-spotify.so"

                "https://login5.spotify.com/v4/login" to "http://127.0.0.1:$port/v4/login" inFile
                        "lib/$architecture/liborbit-jni-spotify.so"
            }
        })
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

        firstLoginScreenRenderFingerprint.method.apply {
            val onEventIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_INTERFACE && getReference<MethodReference>()?.name == "getView"
            }

            val buttonRegister = getInstruction<OneRegisterInstruction>(onEventIndex + 1).registerA

            addInstruction(
                onEventIndex + 2,
                """
                    invoke-static {v$buttonRegister}, $EXTENSION_CLASS_DESCRIPTOR->setLoginButton(Landroid/view/View;)V
                    """
            )

            /*
            val returnIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.RETURN_VOID
            }

            replaceInstruction(
                returnIndex,
                """
                    invoke-static {}, $EXTENSION_CLASS_HELPER->getButton()Landroid/view/View;
                    """
            )

            addInstructions(
                returnIndex + 1,
                """
                    move-result-object v0
                    invoke-virtual {v0}, Landroid/view/View;->performClick()Z
                    return-void
                """
            )
             */
        }

        secondLoginScreenRenderFingerprint.method.apply {
            val getViewIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_INTERFACE && getReference<MethodReference>()?.name == "getView"
            }

            val buttonRegister = getInstruction<OneRegisterInstruction>(getViewIndex + 1).registerA

            // Early returning the render for loop since the first item of the loop is the login button
            addInstructions(
                getViewIndex + 2,
                """
                    invoke-virtual {v$buttonRegister}, Landroid/view/View;->performClick()Z
                    return-void
                    """
            )
        }

        thirdLoginScreenRenderFingerprint.method.apply {
            val invokeSetListenerIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_VIRTUAL &&
                        getReference<MethodReference>()?.definingClass == "Landroid/view/View;" &&
                        getReference<MethodReference>()?.name == "setOnClickListener"
            }

            val buttonRegister = getInstruction<FiveRegisterInstruction>(invokeSetListenerIndex).registerC

            addInstruction(
                invokeSetListenerIndex + 1,
                """
                    invoke-virtual {v$buttonRegister}, Landroid/view/View;->performClick()Z
                    """
            )
        }

        thirdLoginOnClickFingerprint.method.apply {
            // Return void is NOT at the end of the method
            val loginActionIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.RETURN_VOID
            } - 1

            val loginActionInstruction = getInstruction<FiveRegisterInstruction>(loginActionIndex)

            addInstructions(
                loginActionIndex,
                """
                    const-string v${loginActionInstruction.registerD}, "bogus"
                    const-string v${loginActionInstruction.registerE}, "bogus"
                    """
            )
        }

    }
}
