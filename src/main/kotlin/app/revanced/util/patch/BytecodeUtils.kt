package app.revanced.util.patch

import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction

/**
 * @return the first constant instruction with the resource id, or -1 if not found.
 */
fun Method.indexOfFirstConstantInstruction(constantValue: Long): Int {
    return implementation?.let {
        it.instructions.indexOfFirst { instruction ->
            instruction.opcode == Opcode.CONST && (instruction as WideLiteralInstruction).wideLiteral == constantValue
        }
    } ?: -1
}