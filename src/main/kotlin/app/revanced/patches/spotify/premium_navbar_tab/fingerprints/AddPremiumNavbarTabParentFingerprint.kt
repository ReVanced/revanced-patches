package app.revanced.patches.spotify.premium_navbar_tab.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.spotify.premium_navbar_tab.annotations.PremiumNavbarTabCompatibility

@Name("add-premium-navbar-tab-parent-fingerprint")
@Version("0.0.1")
@PremiumNavbarTabCompatibility
object AddPremiumNavbarTabParentFingerprint : MethodFingerprint(
     strings = listOf("com.samsung.android.samsungaccount.action.REQUEST_AUTHCODE")
)