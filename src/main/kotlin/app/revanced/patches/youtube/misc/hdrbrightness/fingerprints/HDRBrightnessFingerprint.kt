package app.revanced.patches.youtube.misc.hdrbrightness.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.Opcode

object HDRBrightnessFingerprint : MethodFingerprint(
    "V",
    opcodes = listOf(Opcode.CMPL_FLOAT),
    strings = listOf("c.SettingNotFound;", "screen_brightness", "android.mediaview"),
)