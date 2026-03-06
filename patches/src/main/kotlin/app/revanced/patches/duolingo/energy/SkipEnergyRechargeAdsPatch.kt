package app.revanced.patches.duolingo.energy

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val skipEnergyRechargeAdsPatch = bytecodePatch(
    name = "Skip energy recharge ads",
    description = "Skips watching ads to recharge energy."
) {
    compatibleWith("com.duolingo")

    execute {
        initializeEnergyConfigFingerprint
            .match(energyConfigToStringFingerprint.classDef)
            .method.apply {
                val toStringMethod = energyConfigToStringFingerprint.method
                val instructions = toStringMethod.implementation!!.instructions.toList()

                var energyField: String? = null

                // Search for the string "energy=" and get the preceding IGET instruction.
                for (i in instructions.indices) {
                    val instr = instructions[i]
                    val ref = (instr as? ReferenceInstruction)?.reference
                    
                    if (ref is StringReference && ref.string.contains("energy=")) {
                        // Search backwards for the field getter (IGET)
                        for (j in i downTo 0) {
                            val prevInstr = instructions[j]
                            if (prevInstr.opcode == Opcode.IGET) {
                                val fieldRef = (prevInstr as ReferenceInstruction).reference as FieldReference
                                // Construct the full valid Smali field reference: Lclass;->name:type
                                energyField = "${fieldRef.definingClass}->${fieldRef.name}:${fieldRef.type}"
                                break
                            }
                        }
                        break
                    }
                }

                // Fallback: The first IGET instruction is guaranteed to be the 'energy' property.
                if (energyField == null) {
                    val fallbackInstr = instructions.first { it.opcode == Opcode.IGET }
                    val fieldRef = (fallbackInstr as ReferenceInstruction).reference as FieldReference
                    energyField = "${fieldRef.definingClass}->${fieldRef.name}:${fieldRef.type}"
                }

                val insertIndex = initializeEnergyConfigFingerprint.patternMatch!!.startIndex

                addInstructions(
                    insertIndex,
                    """
                        const/16 v0, 99
                        iput v0, p0, $energyField
                    """
                )
            }
    }
}
