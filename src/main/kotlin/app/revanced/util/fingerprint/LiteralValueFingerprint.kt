package app.revanced.util.fingerprint

import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.util.containsWideLiteralInstructionIndex
import com.android.tools.smali.dexlib2.Opcode

/**
 * A fingerprint to resolve methods that contain a specific literal value.
 *
 * @param returnType The method's return type compared using String.startsWith.
 * @param accessFlags The method's exact access flags using values of AccessFlags.
 * @param parameters The parameters of the method. Partial matches allowed and follow the same rules as returnType.
 * @param opcodes An opcode pattern of the method's instructions. Wildcard or unknown opcodes can be specified by null.
 * @param strings A list of the method's strings compared each using String.contains.
 * @param literalSupplier A supplier for the literal value to check for.
 */
abstract class LiteralValueFingerprint(
    returnType: String? = null,
    accessFlags: Int? = null,
    parameters: Iterable<String>? = null,
    opcodes: Iterable<Opcode>? = null,
    strings: Iterable<String>? = null,
    // Has to be a supplier because the fingerprint is created before patches can set literals.
    literalSupplier: () -> Long
) : MethodFingerprint(
    returnType = returnType,
    accessFlags = accessFlags,
    parameters = parameters,
    opcodes = opcodes,
    strings = strings,
    customFingerprint = { methodDef, _ ->
        methodDef.containsWideLiteralInstructionIndex(literalSupplier())
    }
)