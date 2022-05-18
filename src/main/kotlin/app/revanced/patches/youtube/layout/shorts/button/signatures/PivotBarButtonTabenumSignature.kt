package app.revanced.patches.youtube.layout.shorts.button.signatures

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patches.youtube.layout.shorts.button.annotations.ShortsButtonCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("pivotbar-buttons-tabenum-signature")
@MatchingMethod(
    "Lkne", "z"
)
@FuzzyPatternScanMethod(2) // FIXME: Test this threshold and find the best value.
@ShortsButtonCompatibility
@Version("0.0.1")
object PivotBarButtonTabenumSignature : MethodSignature(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf("Z"),
    listOf(
        Opcode.CHECK_CAST,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.IGET,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IGET_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.IGET_OBJECT,
        Opcode.IF_NEZ,
        Opcode.SGET_OBJECT,
        Opcode.IGET,
        Opcode.INVOKE_STATIC, // SomeEnum.fromValue(tabOrdinal)
        Opcode.MOVE_RESULT_OBJECT
    )
)