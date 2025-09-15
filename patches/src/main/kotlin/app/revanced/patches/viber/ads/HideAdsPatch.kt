package app.revanced.patches.viber.ads

import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide Ads",
    description = "Hides ad banners between chats.",
) {
    compatibleWith("com.viber.voip"("25.9.2.0", "26.1.2.0"))

    execute {
        val instructions = findAdStringFingerprint.method.implementation!!.instructions.toList()

        // Find the ads free string index
        val stringIndex = instructions.indexOfFirst { instruction ->
            instruction is Instruction21c &&
            (instruction.reference as? StringReference)?.string == ADS_FREE_STR
        }

        // Find the last TypeReference before the string
        // to get the class (object type) of the no ads field
        val targetClassName = instructions.take(stringIndex)
            .filterIsInstance<Instruction21c>()
            .lastOrNull { it.reference is TypeReference }
            ?.let { (it.reference as TypeReference).type }
            ?: throw PatchException("Could not find obfuscated class for $ADS_FREE_STR")


        // Patch the ads-free method to always return true
        adsFreeFingerprint(targetClassName).method.returnEarly(1)
    }
}
