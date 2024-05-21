package app.revanced.util.patch

import app.revanced.patcher.fingerprint.MethodFingerprintBuilder
import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.util.containsWideLiteralInstructionValue

/**
 * A fingerprint to resolve methods that contain a specific literal value.
 *
 * @param literalSupplier A supplier for the literal value to check for.
 * @param block The fingerprint builder block.
 */
// TODO: Convert literalSupplier to an extension function on MethodFingerprintBuilder.
fun literalValueFingerprint(
    // Has to be a supplier because the fingerprint is created before patches can set literals.
    literalSupplier: () -> Long,
    block: MethodFingerprintBuilder.() -> Unit,
) = methodFingerprint {
    block()

    custom { methodDef, _ ->
        methodDef.containsWideLiteralInstructionValue(literalSupplier())
    }
}
