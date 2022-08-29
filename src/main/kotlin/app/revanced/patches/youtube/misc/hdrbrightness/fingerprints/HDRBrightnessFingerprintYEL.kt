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
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.FieldReference

@Name("hdr-brightness-fingerprint-xxz")
@MatchingMethod(
    "Lyel;", "G"
)
@FuzzyPatternScanMethod(3)
@HDRBrightnessCompatibility
@Version("0.0.1")
object HDRBrightnessFingerprintYEL : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf("I", "I", "I", "I"),
    listOf(
        Opcode.SGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET,
        Opcode.IPUT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL
    ),
    null,
    customFingerprint = { methodDef ->
        methodDef.implementation!!.instructions.any {
            ((it as? ReferenceInstruction)?.reference as? FieldReference)?.let { field ->
                // iput vx, vy, Landroid/view/WindowManager$LayoutParams;->screenBrightness:F
                field.definingClass == "Landroid/view/WindowManager\$LayoutParams;" && field.name == "screenBrightness"
            } == true
        }
    }
)
