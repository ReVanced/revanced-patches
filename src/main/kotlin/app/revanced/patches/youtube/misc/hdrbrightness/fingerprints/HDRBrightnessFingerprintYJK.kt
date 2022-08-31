package app.revanced.patches.youtube.misc.hdrbrightness.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.hdrbrightness.annotations.HDRBrightnessCompatibility
import org.jf.dexlib2.Opcode

@Name("hdr-brightness-fingerprint-xxz")
@MatchingMethod(
    "Lyjk;", "D"
)
@FuzzyPatternScanMethod(3)
@HDRBrightnessCompatibility
@Version("0.0.1")
object HDRBrightnessFingerprintYJK : MethodFingerprint(
    "V",
    opcodes = listOf(
        Opcode.SGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.IGET,
        Opcode.IPUT,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_VIRTUAL
    ),
    strings = listOf("c.SettingNotFound;", "screen_brightness", "android.mediaview"),
)