package app.revanced.patches.youtube.layout.reels.signatures

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patches.youtube.layout.reels.annotations.HideReelsCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("hide-reels-signature")
@MatchingMethod(
    "Ljvy", "<init>"
)
@FuzzyPatternScanMethod(3) // FIXME: Test this threshold and find the best value.
@HideReelsCompatibility
@Version("0.0.1")
object HideReelsSignature : MethodSignature(
    "V", AccessFlags.PUBLIC or AccessFlags.CONSTRUCTOR, listOf(
        "L", "L", "L", "L", "L", "L", "L", "L", "L", "L", "L", "[B", "[B", "[B", "[B", "[B", "[B"
    ), listOf(
        Opcode.MOVE_OBJECT,
        Opcode.MOVE_OBJECT,
        Opcode.INVOKE_DIRECT,
        Opcode.MOVE_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.MOVE_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.MOVE_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.MOVE_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.MOVE_OBJECT,
        Opcode.IPUT_OBJECT,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.IPUT_OBJECT,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.IPUT_OBJECT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_OBJECT,
        Opcode.MOVE_OBJECT_FROM16,
        Opcode.IPUT_OBJECT,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IPUT_OBJECT
    )
)