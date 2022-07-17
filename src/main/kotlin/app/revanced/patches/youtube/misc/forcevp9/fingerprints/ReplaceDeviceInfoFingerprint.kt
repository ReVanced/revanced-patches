package app.revanced.patches.youtube.misc.forcevp9.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.forcevp9.annotations.ForceVP9Compatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.FieldReference

@Name("replace-device-info-parent-fingerprint")
@MatchingMethod(
    "Lvjb;", "e"
)
@DirectPatternScanMethod
@ForceVP9Compatibility
@Version("0.0.1")
object ReplaceDeviceInfoFingerprint : MethodFingerprint(
    "L", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf(), null,
    null,
    customFingerprint = { methodDef ->
        methodDef.implementation!!.instructions.any {
            ((it as? ReferenceInstruction)?.reference as? FieldReference)?.definingClass.equals("Landroid/os/Build;")
                    && ((it as? ReferenceInstruction)?.reference as? FieldReference)?.name.equals("MANUFACTURER")
        }
    }
)