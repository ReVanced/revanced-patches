package app.revanced.patches.youtube.interaction.seekbar.signatures

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patches.youtube.interaction.seekbar.annotation.SeekbarTappingCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("enable-seekbar-tapping-parent")
@MatchingMethod("Lzmx;", "I")
@FuzzyPatternScanMethod(2) // FIXME: Test this threshold and find the best value.
@SeekbarTappingCompatibility
@Version("0.0.1")
object SeekbarTappingParentSignature : MethodSignature(
    "L", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf(), listOf(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.NEW_ARRAY,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_WIDE,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.APUT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_WIDE,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_4,
        Opcode.APUT_OBJECT,
        Opcode.CONST,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.RETURN_OBJECT
    )
)