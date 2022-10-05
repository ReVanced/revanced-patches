package app.revanced.patches.youtube.misc.microg.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility

@Name("google-play-prime-fingerprint")
@MicroGPatchCompatibility
@Version("0.0.1")
object PrimeFingerprint : MethodFingerprint(
    strings = listOf("com.google.android.GoogleCamera", "com.android.vending")
)