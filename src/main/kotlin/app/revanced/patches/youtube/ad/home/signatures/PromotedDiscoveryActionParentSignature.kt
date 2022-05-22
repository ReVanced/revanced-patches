package app.revanced.patches.youtube.ad.home.signatures

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patches.youtube.ad.home.annotation.PromotionsCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("promoted-discovery-app-parent-signature")
@MatchingMethod(
    "Ljqb;", "lG"
)
@FuzzyPatternScanMethod(2) // FIXME: Test this threshold and find the best value.
@PromotionsCompatibility
@Version("0.0.1")
object PromotedDiscoveryActionParentSignature : MethodSignature(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL or AccessFlags.BRIDGE or AccessFlags.SYNTHETIC,
    listOf("L", "L"),
    listOf(
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.CHECK_CAST,
        Opcode.INVOKE_VIRTUAL_RANGE,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.IGET_BOOLEAN,
        Opcode.CONST_4,
        Opcode.XOR_INT_2ADDR,
        Opcode.IGET_BOOLEAN,
        Opcode.INVOKE_DIRECT,
        Opcode.IGET_BOOLEAN,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.CONST_4,
        Opcode.IF_NEZ,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT
    )
)