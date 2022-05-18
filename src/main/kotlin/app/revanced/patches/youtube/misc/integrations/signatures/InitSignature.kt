package app.revanced.patches.youtube.misc.integrations.signatures

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patches.youtube.misc.integrations.annotations.IntegrationsCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("init-signature")
@MatchingMethod(
    "Lacnx", "onCreate"
)
@FuzzyPatternScanMethod(2) // FIXME: Test this threshold and find the best value.
@IntegrationsCompatibility
@Version("0.0.1")
object InitSignature : MethodSignature(
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