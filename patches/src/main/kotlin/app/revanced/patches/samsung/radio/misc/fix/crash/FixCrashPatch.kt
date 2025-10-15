@file:Suppress("unused")

package app.revanced.patches.samsung.radio.misc.fix.crash

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.revanced.patches.samsung.radio.restrictions.device.bypassDeviceChecksPatch
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

private const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/revanced/extension/samsung/radio/misc/fix/crash/FixCrashPatch;"

val fixCrashPatch = bytecodePatch(
    name = "Fix Crashes",
    description = "Stops the app from crashing because of missing system permissions." +
            "This is not required if you plan to install it as a system app.",
) {
    dependsOn(addManifestPermissionsPatch, bypassDeviceChecksPatch, sharedExtensionPatch("samsung/radio"))
    compatibleWith("com.sec.android.app.fm"("12.4.00.7"))

    execute {
        permissionRequestListFingerprint.method.apply {
            var searchIndex = 0
            while (searchIndex < instructions.size) {
                // We search for each "filled-new-array" instruction followed, even not immediately, by "move-result-object", from which we take the index
                val filledNewArrayIndex = indexOfFirstInstruction(searchIndex, Opcode.FILLED_NEW_ARRAY)
                if (filledNewArrayIndex == -1) break // No more filled-new-array instructions found

                val moveResultIndex = indexOfFirstInstruction(filledNewArrayIndex, Opcode.MOVE_RESULT_OBJECT)
                if (moveResultIndex == -1) break // No move-result-object found after the filled-new-array

                // Get the register where the array is saved
                val arrayRegister = getInstruction<OneRegisterInstruction>(moveResultIndex).registerA

                // Invoke the method that we added to the class earlier
                addInstruction(
                    moveResultIndex + 1,
                    "invoke-static {v$arrayRegister}, ${EXTENSION_CLASS_DESCRIPTOR}->fixPermissionRequestList([Ljava/lang/String;)[Ljava/lang/String;"
                )
                addInstruction(
                    moveResultIndex + 2, "move-result-object v$arrayRegister"
                )

                searchIndex = moveResultIndex + 1 + 2 // 2 = number of instructions we added
            }
        }

        checkCallStateFingerprint.method.apply {
            addInstruction(0, "const/4 v0, 0x0")
            addInstruction(1, "return v0")
        }
    }
}