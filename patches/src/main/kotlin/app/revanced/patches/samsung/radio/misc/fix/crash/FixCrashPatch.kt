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

private const val PERMISSION_POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"
private const val PERMISSION_RECORD_AUDIO = "android.permission.RECORD_AUDIO"
private const val PERMISSION_READ_PHONE_STATE = "android.permission.READ_PHONE_STATE"
private const val PERMISSION_FOREGROUND_SERVICE_MICROPHONE = "android.permission.FOREGROUND_SERVICE_MICROPHONE"

val fixCrashPatch = bytecodePatch(
    name = "Fix Crashes",
    description = "Stops the app from crashing because of missing system permissions.",
) {
    dependsOn(addManifestPermissionsPatch, bypassDeviceChecksPatch, sharedExtensionPatch())
    sharedExtensionPatch()
    compatibleWith("com.sec.android.app.fm")

    execute {
        // Use a helper method to avoid the need of picking out multiple free registers from the hooked code.
        permissionRequestListFingerprint.classDef.methods.add(
            ImmutableMethod(
                permissionRequestListFingerprint.classDef.type,
                "__fixPermissionRequestList",
                listOf(ImmutableMethodParameter("[Ljava/lang/String;", null, "p0")),
                "[Ljava/lang/String;",
                AccessFlags.PRIVATE.value or AccessFlags.FINAL.value or AccessFlags.STATIC.value,
                null,
                null,
                MutableMethodImplementation(9),
            ).toMutable().apply {
                addInstructionsWithLabels(
                    0, """
                            # Permission to find
                            const-string v0, "$PERMISSION_POST_NOTIFICATIONS"

                            # Check if the array contains the permission
                            invoke-static {p0, v0}, Lapp/revanced/extension/shared/Utils;->arrayContains([Ljava/lang/Object;Ljava/lang/Object;)Z
                            move-result v0
                            if-eqz v0, :check_record_audio

                            # Array of permissions to add
                            const-string v0, "$PERMISSION_RECORD_AUDIO"
                            const-string v1, "$PERMISSION_READ_PHONE_STATE"
                            const-string v2, "$PERMISSION_FOREGROUND_SERVICE_MICROPHONE"
                            filled-new-array {v0, v1, v2}, [Ljava/lang/String;
                            move-result-object v1

                            # Merge the two arrays
                            invoke-static {p0, v1}, Lapp/revanced/extension/shared/Utils;->mergeArrays([Ljava/lang/Object;[Ljava/lang/Object;)[Ljava/lang/Object;
                            move-result-object p0
                            check-cast p0, [Ljava/lang/String;

                            :check_record_audio
                            # Permission to find
                            const-string v0, "$PERMISSION_RECORD_AUDIO"

                            # Check if the array contains the permission
                            invoke-static {p0, v0}, Lapp/revanced/extension/shared/Utils;->arrayContains([Ljava/lang/Object;Ljava/lang/Object;)Z
                            move-result v0
                            if-eqz v0, :end

                            # Array of permissions to add
                            const-string v1, "$PERMISSION_FOREGROUND_SERVICE_MICROPHONE"
                            filled-new-array {v1}, [Ljava/lang/String;
                            move-result-object v1

                            # Merge the two arrays
                            invoke-static {p0, v1}, Lapp/revanced/extension/shared/Utils;->mergeArrays([Ljava/lang/Object;[Ljava/lang/Object;)[Ljava/lang/Object;
                            move-result-object p0
                            check-cast p0, [Ljava/lang/String;

                            :end
                            return-object p0
                        """
                )
            })

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
                    "invoke-static {v$arrayRegister}, ${definingClass}->__fixPermissionRequestList([Ljava/lang/String;)[Ljava/lang/String;"
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