package app.revanced.util.patch

import app.revanced.extensions.containsConstantInstructionValue
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

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
        methodDef.containsConstantInstructionValue(literalSupplier())
    }
)