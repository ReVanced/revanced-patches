package app.revanced.patches.youtube.layout.hidetimeandseekbar.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.hidetimeandseekbar.annotations.HideTimeAndSeekbarCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("time-counter-fingerprint")
@MatchingMethod("Lfez", "a")
@FuzzyPatternScanMethod(3)
@HideTimeAndSeekbarCompatibility
@Version("0.0.1")
object TimeCounterFingerprint : MethodFingerprint(
    "L", AccessFlags.PUBLIC or AccessFlags.STATIC, listOf("J"), listOf(
        Opcode.SGET_OBJECT,
        Opcode.CONST_WIDE_16,
        Opcode.ADD_LONG_2ADDR,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_WIDE,
    )
)