@file:Suppress("unused")

package app.revanced.patches.samsung.radio.misc.fix.crash

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.samsung.radio.restrictions.device.bypassDeviceChecksPatch
import app.revanced.util.findInstructionIndicesReversedOrThrow
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/samsung/radio/misc/fix/crash/FixCrashPatch;"

val fixCrashPatch = bytecodePatch(
    name = "Fix crashes", description = "Prevents the app from crashing because of missing system permissions."
) {
    dependsOn(addManifestPermissionsPatch, bypassDeviceChecksPatch)
    extendWith("extensions/samsung/radio.rve")
    compatibleWith("com.sec.android.app.fm"("12.4.00.7", "12.3.00.13", "12.3.00.11"))

    execute {
        permissionRequestListFingerprint.method.apply {
            findInstructionIndicesReversedOrThrow(Opcode.FILLED_NEW_ARRAY).forEach { filledNewArrayIndex ->
                val moveResultIndex = indexOfFirstInstruction(filledNewArrayIndex, Opcode.MOVE_RESULT_OBJECT)
                if (moveResultIndex < 0) return@forEach // No move-result-object found after the filled-new-array

                // Get the register where the array is saved
                val arrayRegister = getInstruction<OneRegisterInstruction>(moveResultIndex).registerA

                // Invoke the method from the extension
                addInstructions(
                    moveResultIndex + 1, """
                        invoke-static { v$arrayRegister }, ${EXTENSION_CLASS_DESCRIPTOR}->fixPermissionRequestList([Ljava/lang/String;)[Ljava/lang/String;
                        move-result-object v$arrayRegister
                    """
                )
            }
        }
    }
}