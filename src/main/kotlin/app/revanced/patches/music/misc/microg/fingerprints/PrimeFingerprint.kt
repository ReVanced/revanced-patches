package app.revanced.patches.music.misc.microg.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.music.misc.microg.annotations.MusicMicroGPatchCompatibility

@Name("google-play-prime-fingerprint")

@MusicMicroGPatchCompatibility
@Version("0.0.1")
object PrimeFingerprint : MethodFingerprint(
    strings = listOf("com.google.android.GoogleCamera", "com.android.vending")
)