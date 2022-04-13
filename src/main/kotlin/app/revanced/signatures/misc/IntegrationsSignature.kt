package app.revanced.signatures.misc

import app.revanced.patcher.signature.*
import app.revanced.signatures.SignatureSupplier
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

class IntegrationsSignature : SignatureSupplier {
    override fun get() = MethodSignature(
        "integrations-patch",
        SignatureMetadata(
            method = MethodMetadata(
                definingClass = "???", // TODO: Fill this in.
                methodName = "???", // TODO: Fill this in.
                comment = "YouTube v17.03.38"
            ),
            patcher = PatcherMetadata(
                // FIXME: Test this threshold and find the best value.
                resolverMethod = ResolverMethod.Fuzzy(2)
            )
        ),
        "V",
        AccessFlags.PUBLIC.value,
        listOf(),
        listOf(
            Opcode.SGET_OBJECT,
            Opcode.NEW_INSTANCE,
            Opcode.INVOKE_DIRECT,
            Opcode.IGET_OBJECT,
            Opcode.CONST_STRING,
            Opcode.IF_NEZ,
            Opcode.IGET_OBJECT,
            Opcode.IGET_OBJECT,
            Opcode.IGET_OBJECT,
            Opcode.IGET_OBJECT,
            Opcode.MOVE_OBJECT,
            Opcode.CHECK_CAST,
            Opcode.MOVE_OBJECT,
            Opcode.CHECK_CAST,
            Opcode.CONST_4,
            Opcode.CONST_STRING,
            Opcode.INVOKE_INTERFACE_RANGE,
            Opcode.MOVE_RESULT_OBJECT,
            Opcode.INVOKE_STATIC,
            Opcode.MOVE_RESULT_OBJECT,
            Opcode.SPUT_OBJECT,
            Opcode.SGET_OBJECT,
            Opcode.INVOKE_STATIC,
            Opcode.INVOKE_STATIC,
            Opcode.MOVE_RESULT_OBJECT,
            Opcode.IGET_OBJECT,
            Opcode.INVOKE_INTERFACE,
            Opcode.MOVE_RESULT_OBJECT,
            Opcode.CHECK_CAST,
            Opcode.INVOKE_VIRTUAL,
            Opcode.INVOKE_SUPER,
            Opcode.INVOKE_VIRTUAL
        )
    )
}