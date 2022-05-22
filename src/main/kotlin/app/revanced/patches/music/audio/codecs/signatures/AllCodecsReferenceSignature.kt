package app.revanced.patches.music.audio.codecs.signatures

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patches.music.audio.codecs.annotations.CodecsUnlockCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("all-codecs-reference-signature")
@MatchingMethod(
    "Laari;",
    "b",
)
@FuzzyPatternScanMethod(2) // FIXME: Test this threshold and find the best value.
@CodecsUnlockCompatibility
@Version("0.0.1")
object AllCodecsReferenceSignature : MethodSignature(
    "J", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf("L"), listOf(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
        Opcode.CONST_4,
        Opcode.IF_EQZ,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_NEZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.IPUT_BOOLEAN,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.GOTO,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
        Opcode.IF_NEZ,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_NEZ,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.IPUT_BOOLEAN,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.GOTO,
        Opcode.MOVE_EXCEPTION,
        Opcode.INVOKE_SUPER,
        Opcode.MOVE_RESULT_WIDE,
        Opcode.RETURN_WIDE
    ), listOf("itag")
)