package app.revanced.patches.spotify.premium_navbar_tab.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.spotify.premium_navbar_tab.annotations.PremiumNavbarTabCompatibility

@Name("premium-navbar-fingerprint")
@Version("0.0.1")
@PremiumNavbarTabCompatibility
object AddPremiumNavbarTabFingerprint : MethodFingerprint(
    parameters = listOf("L", "L", "L", "L", "L", "L")
)