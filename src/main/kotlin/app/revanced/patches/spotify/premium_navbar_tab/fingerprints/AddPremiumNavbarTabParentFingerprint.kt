package app.revanced.patches.spotify.premium_navbar_tab.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object AddPremiumNavbarTabParentFingerprint : MethodFingerprint(
     strings = listOf("com.samsung.android.samsungaccount.action.REQUEST_AUTHCODE")
)