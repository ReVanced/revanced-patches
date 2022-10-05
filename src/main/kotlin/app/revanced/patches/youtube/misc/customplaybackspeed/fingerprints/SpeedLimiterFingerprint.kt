package app.revanced.patches.youtube.misc.customplaybackspeed.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.customplaybackspeed.annotations.CustomPlaybackSpeedCompatibility
import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.Opcode

@Name("speed-limiter-fingerprint")
@FuzzyPatternScanMethod(2)
@CustomPlaybackSpeedCompatibility
@Version("0.0.1")
object SpeedLimiterFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PUBLIC or AccessFlags.FINAL,
    listOf("F"),
    listOf(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
        Opcode.CONST_HIGH16,
        Opcode.GOTO,
        Opcode.CONST_HIGH16,
        Opcode.CONST_HIGH16,
        Opcode.INVOKE_STATIC,
    ),
)
