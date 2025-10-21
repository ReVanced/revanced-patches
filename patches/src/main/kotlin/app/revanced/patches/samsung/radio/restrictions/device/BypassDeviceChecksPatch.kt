package app.revanced.patches.samsung.radio.restrictions.device

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/revanced/extension/samsung/radio/restrictions/device/BypassDeviceChecksPatch;"

@Suppress("unused")
val bypassDeviceChecksPatch = bytecodePatch(
    name = "Bypass device checks",
    description = "Removes firmware and region blacklisting. " +
            "This patch will still not allow the app to run on devices that do not have the required hardware.",
) {
    extendWith("extensions/samsung/radio.rve")
    compatibleWith("com.sec.android.app.fm"("12.4.00.7", "12.3.00.13", "12.3.00.11"))

    execute {
        // Return false = The device is not blacklisted
        checkDeviceFingerprint.method.apply {
            // Find the first string that start with "SM-", that's the list of incompatible devices
            val firstStringIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.CONST_STRING &&
                        getReference<StringReference>()?.string?.startsWith("SM-") == true
            }

            // Find the following filled-new-array (or filled-new-array/range) instruction
            val filledNewArrayIndex = indexOfFirstInstructionOrThrow(firstStringIndex + 1) {
                opcode == Opcode.FILLED_NEW_ARRAY || opcode == Opcode.FILLED_NEW_ARRAY_RANGE
            }

            // Find an available register for our use
            val resultRegister = findFreeRegister(filledNewArrayIndex + 1)

            // Store the array there and invoke the method that we added to the class earlier
            addInstructions(
                filledNewArrayIndex + 1, """
                move-result-object v$resultRegister
                invoke-static { v$resultRegister }, $EXTENSION_CLASS_DESCRIPTOR->checkIfDeviceIsIncompatible([Ljava/lang/String;)Z
                move-result v$resultRegister
                return v$resultRegister
            """
            )

            // Remove the instructions before our strings
            removeInstructions(0, firstStringIndex)
        }
    }
}
