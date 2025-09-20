package app.revanced.patches.viber.ads

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.fingerprint
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.indexOfFirstInstructionReversedOrThrow
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide Ads",
    description = "Hides ad banners between chats.",
) {
    compatibleWith("com.viber.voip"("25.9.2.0", "26.1.2.0"))

    execute {
        val method = findAdStringFingerprint.method
 
        // Find the ads free string index
        val stringIndex = findAdStringFingerprint.stringMatches!!.first().index

        // Search backwards from the string to find the `new-instance` (TypeReference) instruction
        val typeRefIndex = method.indexOfFirstInstructionReversedOrThrow(stringIndex) { this.opcode == Opcode.NEW_INSTANCE }

        // Get the class name from the TypeReference
        val targetClass = method.getInstruction<ReferenceInstruction>(typeRefIndex).reference as TypeReference

        // Patch the ads-free method to always return true
        fingerprint {
            returns("I")
            parameters()
            custom { method, classDef ->
                classDef == targetClass
            }
        }.method.returnEarly(1)
    }
}
