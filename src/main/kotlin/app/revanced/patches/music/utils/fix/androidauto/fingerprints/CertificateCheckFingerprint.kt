package app.revanced.patches.music.utils.fix.androidauto.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object CertificateCheckFingerprint : MethodFingerprint(
    returnType = "Z",
    parameters = listOf("L"),
    strings = listOf("X509")
)