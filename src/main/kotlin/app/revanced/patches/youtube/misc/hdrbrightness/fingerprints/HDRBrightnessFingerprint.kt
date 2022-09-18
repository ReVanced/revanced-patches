package app.revanced.patches.youtube.misc.hdrbrightness.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.hdrbrightness.annotations.HDRBrightnessCompatibility

@Name("hdr-brightness-fingerprint")
@MatchingMethod(
    "Lyls;", "c"
)
@HDRBrightnessCompatibility
@Version("0.0.1")
object HDRBrightnessFingerprint : MethodFingerprint(
    "V",
    strings = listOf("c.SettingNotFound;", "screen_brightness", "android.mediaview", "lux"),
)