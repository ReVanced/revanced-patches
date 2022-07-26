package app.revanced.patches.music.misc.microg.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patches.music.misc.microg.annotations.MusicMicroGPatchCompatibility

@Name("google-play-prime-fingerprint")
@MatchingMethod(
    "Lrwi;", "a"
)
@DirectPatternScanMethod
@MusicMicroGPatchCompatibility
@Version("0.0.1")
object PrimeFingerprint : MethodFingerprint(
    null, null, null, null, listOf("com.google.android.GoogleCamera", "com.android.vending")
)