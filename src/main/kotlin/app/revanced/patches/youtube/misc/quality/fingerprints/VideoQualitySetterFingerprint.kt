
package app.revanced.patches.youtube.misc.quality.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.quality.annotations.DefaultVideoQualityCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("video-quality-setter-fingerprint")
@MatchingMethod(
    "Lkec;", "a"
)
@DirectPatternScanMethod
@DefaultVideoQualityCompatibility
@Version("0.0.1")
object VideoQualitySetterFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf("[L", "I", "I", "Z", "I"),
    listOf(
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IPUT_BOOLEAN,
    )
)