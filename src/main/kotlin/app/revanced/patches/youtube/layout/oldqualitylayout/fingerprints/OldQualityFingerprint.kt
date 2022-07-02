package app.revanced.patches.youtube.layout.oldqualitylayout.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.oldqualitylayout.annotations.OldQualityLayoutCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("old-quality-fingerprint")
@MatchingMethod(definingClass = "Libh")
@FuzzyPatternScanMethod(2)
@OldQualityLayoutCompatibility
@Version("0.0.1")
object OldQualityFingerprint : MethodFingerprint(
    "L", AccessFlags.FINAL or AccessFlags.PRIVATE, listOf("Z"), listOf(
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.IGET_OBJECT,
        Opcode.GOTO,
        Opcode.IGET_OBJECT,
    )
)