package app.revanced.patches.tasker.license.unlock.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object CheckLicenseFingerprint : MethodFingerprint(
    strings = listOf("Can't check license")
)