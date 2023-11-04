package app.revanced.patches.spotify.navbar.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object AddPremiumNavbarTabFingerprint : MethodFingerprint(
    parameters = listOf("L", "L", "L", "L", "L", "L")
)