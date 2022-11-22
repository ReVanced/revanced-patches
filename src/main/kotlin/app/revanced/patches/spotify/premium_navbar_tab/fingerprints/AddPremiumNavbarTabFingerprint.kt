package app.revanced.patches.spotify.premium_navbar_tab.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object AddPremiumNavbarTabFingerprint : MethodFingerprint(
    parameters = listOf("L", "L", "L", "L", "L", "L")
)