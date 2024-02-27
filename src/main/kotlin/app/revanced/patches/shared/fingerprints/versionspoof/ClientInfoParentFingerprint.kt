package app.revanced.patches.shared.fingerprints.versionspoof

import app.revanced.patcher.fingerprint.MethodFingerprint

object ClientInfoParentFingerprint : MethodFingerprint(
    returnType = "V",
    strings = listOf("Android Wear")
)
