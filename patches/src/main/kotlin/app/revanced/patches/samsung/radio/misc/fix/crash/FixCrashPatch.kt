@file:Suppress("unused")

package app.revanced.patches.samsung.radio.misc.fix.crash

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.samsung.radio.restrictions.device.bypassDeviceChecksPatch
import app.revanced.util.findFreeRegister
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference


val fixCrashPatch = bytecodePatch(
    name = "Fix Crashes",
    description = "Stops the app from crashing because of missing system permissions.",
) {
    dependsOn(addManifestPermissionsPatch)
    dependsOn(bypassDeviceChecksPatch)
    compatibleWith("com.sec.android.app.fm")

    execute {
        permissionRequestListFingerprint.method.apply {
            // Search for the "android.permission.POST_NOTIFICATIONS" string and get its register
            val notificationPermStrIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.CONST_STRING && getReference<StringReference>()?.string == "android.permission.POST_NOTIFICATIONS"
            }
            val notificationPermRegister = getInstruction<OneRegisterInstruction>(notificationPermStrIndex).registerA

            // Find the next "filled-new-array" instruction after it
            var filledNewArrayIndex = indexOfFirstInstructionOrThrow(notificationPermStrIndex) {
                opcode == Opcode.FILLED_NEW_ARRAY
            }

            // Find a free register to load our new permission string
            val phoneStatePermRegister = findFreeRegister(filledNewArrayIndex)
            addInstruction(
                notificationPermStrIndex,
                "const-string v$phoneStatePermRegister, \"android.permission.READ_PHONE_STATE\""
            )

            // Find the new index of "filled-new-array" and replace it with our new array
            filledNewArrayIndex = indexOfFirstInstructionOrThrow(notificationPermStrIndex) {
                opcode == Opcode.FILLED_NEW_ARRAY
            }
            replaceInstruction(
                filledNewArrayIndex,
                "filled-new-array {v$notificationPermRegister, v$phoneStatePermRegister}, [Ljava/lang/String;"
            )
        }

        checkCallStateFingerprint.method.apply {
            // Return false = The user is not on a call
            addInstruction(0, "const/4 v0, 0x0")
            addInstruction(1, "return v0")
        }
    }
}
