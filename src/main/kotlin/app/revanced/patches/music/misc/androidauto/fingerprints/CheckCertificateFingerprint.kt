package app.revanced.patches.music.misc.androidauto.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object CheckCertificateFingerprint : MethodFingerprint(
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    strings = listOf("X509", "Failed to get public key.", "Failed to get certificate.")
)