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

@Name("hdr-brightness-fingerprint-ghz")
@MatchingMethod(
    "Lghz;", "g"
)
@FuzzyPatternScanMethod(3)
@HDRBrightnessCompatibility
@Version("0.0.1")
object HDRBrightnessFingerprintGHZ : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.FINAL, null,
    listOf(
        /* WindowManager.LayoutParams lp = br.getWindow().getAttributes();
         * lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
         * br.getWindow().setAttributes(lp);
         */
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CONST_HIGH16,
        Opcode.IPUT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL
    ),
    null,
    customFingerprint = { methodDef ->
        methodDef.implementation!!.instructions.count() == 16 && methodDef.implementation!!.instructions.any {
            ((it as? NarrowLiteralInstruction)?.narrowLiteral == (/*BRIGHTNESS_OVERRIDE_FULL*/ 1.0f).toRawBits())
        }
    }
)
