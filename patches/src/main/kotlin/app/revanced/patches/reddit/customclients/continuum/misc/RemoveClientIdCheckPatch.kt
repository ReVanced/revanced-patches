package app.revanced.patches.reddit.customclients.continuum.misc

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

internal val clientIdCheckFingerprint = fingerprint {
    strings("client_id_pref_key")
}

@Suppress("unused")
val removeClientIdCheckPatch = bytecodePatch(
    name = "Remove client ID check",
    description = "Removes the dialog that prevents login with default client ID",
) {
    compatibleWith("org.cygnusx1.continuum", "org.cygnusx1.continuum.debug")

    execute {
        // Find all classes and methods containing "client_id_pref_key"
        var targetMethod: com.android.tools.smali.dexlib2.iface.Method? = null
        var targetClassDef: com.android.tools.smali.dexlib2.iface.ClassDef? = null

        var methodsWithClientIdPrefKey = 0
        var methodsWithDialog = 0

        classes.forEach { classDef ->
            classDef.methods.forEach { method ->
                // Check if method contains "client_id_pref_key" string
                val hasClientIdPrefKey = method.implementation?.instructions?.any { instruction ->
                    instruction.getReference<StringReference>()?.string == "client_id_pref_key"
                } == true

                if (hasClientIdPrefKey) {
                    methodsWithClientIdPrefKey++

                    // Check if this method also has MaterialAlertDialogBuilder
                    val hasDialog = method.implementation?.instructions?.any {
                        it.opcode == Opcode.NEW_INSTANCE &&
                        it.getReference<TypeReference>()?.toString()?.contains("MaterialAlertDialogBuilder") == true
                    } == true

                    if (hasDialog) {
                        methodsWithDialog++
                        targetMethod = method
                        targetClassDef = classDef
                        return@forEach
                    }
                }
            }
            if (targetMethod != null) return@forEach
        }

        if (targetMethod == null || targetClassDef == null) {
            throw Exception("Could not find MainActivity method with client ID check")
        }

        // Now patch the method
        val mutableMethod = proxy(targetClassDef).mutableClass
            .methods.first { it.name == targetMethod!!.name && it.parameterTypes == targetMethod!!.parameterTypes }


        mutableMethod.apply {
            // First find where "client_id_pref_key" is loaded
            val clientIdPrefKeyIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.CONST_STRING &&
                getReference<StringReference>()?.string == "client_id_pref_key"
            }

            // Find the String.equals call that comes AFTER the client_id_pref_key reference
            val equalsIndex = indexOfFirstInstructionOrThrow(clientIdPrefKeyIndex) {
                opcode == Opcode.INVOKE_VIRTUAL &&
                getReference<MethodReference>()?.toString()?.contains("Ljava/lang/String;->equals") == true
            }

            // The next instruction is move-result
            val moveResultIndex = equalsIndex + 1

            // After that is if-eqz that branches on the equals result
            val ifEqzIndex = moveResultIndex + 1

            // Verify that this if-eqz is followed by MaterialAlertDialogBuilder
            // to ensure we're removing the right code
            val dialogCheck = indexOfFirstInstructionOrThrow(ifEqzIndex) {
                opcode == Opcode.NEW_INSTANCE &&
                getReference<TypeReference>()?.toString()?.contains("MaterialAlertDialogBuilder") == true
            }

            // Find the iget-object that loads this$0 - work backwards from equals
            // Try to find it by looking for iget-object that references MainActivity
            var igetObjectIndex = -1
            try {
                igetObjectIndex = indexOfFirstInstructionReversedOrThrow(equalsIndex - 1) {
                    opcode == Opcode.IGET_OBJECT &&
                    (toString().contains("this\$0") || toString().contains("MainActivity"))
                }
            } catch (e: Exception) {
                // If we can't find MainActivity reference, just find the first iget-object
                igetObjectIndex = indexOfFirstInstructionReversedOrThrow(equalsIndex - 1) {
                    opcode == Opcode.IGET_OBJECT
                }
            }

            // Find where the normal flow resumes (new Intent creation for LoginActivity)
            // We need to find the Intent that comes AFTER the dialog code
            // Look for the MaterialAlertDialogBuilder first, then find Intent after it
            val dialogBuilderIndex = indexOfFirstInstructionOrThrow(ifEqzIndex) {
                opcode == Opcode.NEW_INSTANCE &&
                getReference<TypeReference>()?.toString()?.contains("MaterialAlertDialogBuilder") == true
            }

            val intentIndex = indexOfFirstInstructionOrThrow(dialogBuilderIndex) {
                opcode == Opcode.NEW_INSTANCE &&
                getReference<TypeReference>()?.toString()?.contains("Landroid/content/Intent;") == true
            }

            // Double-check: look at a few instructions after to see if it's LoginActivity
            val nextInstruction = getInstruction(intentIndex + 1)

            // So remove from igetObjectIndex to intentIndex (exclusive)
            val count = intentIndex - igetObjectIndex
            removeInstructions(igetObjectIndex, count)
        }
    }
}
