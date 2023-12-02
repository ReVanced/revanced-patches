package app.revanced.patches.youtube.video.hdrbrightness.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.Opcode

internal object HDRBrightnessFingerprint : MethodFingerprint(
    "V",
    opcodes = listOf(Opcode.CMPL_FLOAT),
    strings = listOf("c.SettingNotFound;", "screen_brightness", "android.mediaview"),
)