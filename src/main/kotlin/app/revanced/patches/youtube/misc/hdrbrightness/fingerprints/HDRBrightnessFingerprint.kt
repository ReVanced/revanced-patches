package app.revanced.patches.youtube.misc.hdrbrightness.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.hdrbrightness.annotations.HDRBrightnessCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction

@Name("hdrbrightness-fingerprint")
@MatchingMethod(
    "Lghz;", "mZ"
)
@FuzzyPatternScanMethod(3)
@HDRBrightnessCompatibility
@Version("0.0.1")
object HDRBrightnessFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.FINAL, null,
    listOf(
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_HIGH16,
        Opcode.IPUT,
        Opcode.INVOKE_VIRTUAL
    ),
    null,
    customFingerprint = { methodDef ->
        methodDef.implementation!!.instructions.count() == 16 && methodDef.implementation!!.instructions.any {((it as? NarrowLiteralInstruction)?.narrowLiteral == (-1.0f).toRawBits())}
    }
)