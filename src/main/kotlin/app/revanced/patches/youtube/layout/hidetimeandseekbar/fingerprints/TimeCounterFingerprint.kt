package app.revanced.patches.youtube.layout.hidetimeandseekbar.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.hidetimeandseekbar.annotations.HideTimeAndSeekbarCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("time-counter-fingerprint")
@FuzzyPatternScanMethod(3)
@HideTimeAndSeekbarCompatibility
@Version("0.0.1")
object TimeCounterFingerprint : MethodFingerprint(
    "V", AccessFlags.PUBLIC or AccessFlags.FINAL, listOf(), listOf(
        Opcode.IGET_OBJECT,
        Opcode.IGET_WIDE,
        Opcode.CONST_WIDE_16,
        Opcode.CMP_LONG,
        Opcode.IF_LEZ,
        Opcode.IGET_OBJECT,
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.GOTO,
    )
)