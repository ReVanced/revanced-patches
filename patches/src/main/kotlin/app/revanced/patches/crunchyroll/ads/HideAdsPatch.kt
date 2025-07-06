package app.revanced.patches.crunchyroll.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import app.revanced.util.removeFlags
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads"
) {
    compatibleWith("com.crunchyroll.crunchyroid")

    execute {
        // Get obfuscated "enableAds" field from toString method.
        val enableAdsField = videoUrlReadyToStringFingerprint.let {
            val strIndex = videoUrlReadyToStringFingerprint.stringMatches!!.last().index
            val fieldIndex = it.method.indexOfFirstInstruction(strIndex, Opcode.IGET_BOOLEAN)
            it.method.getInstruction<ReferenceInstruction>(fieldIndex).getReference<FieldReference>()!!
        }

        // Remove final access flag on field.
        videoUrlReadyToStringFingerprint.classDef.fields
            .first { it.name == enableAdsField.name }
            .removeFlags(AccessFlags.FINAL)

        // Override enableAds field in non-default constructor.
        val constructor = videoUrlReadyToStringFingerprint.classDef.methods.first {
            AccessFlags.CONSTRUCTOR.isSet(it.accessFlags) && it.parameters.isNotEmpty()
        }
        constructor.addInstructions(
            constructor.instructions.count() - 1,
            """
                move-object/from16 v0, p0
                const/4 v1, 0x0
                iput-boolean v1, v0, $enableAdsField
            """)
    }
}