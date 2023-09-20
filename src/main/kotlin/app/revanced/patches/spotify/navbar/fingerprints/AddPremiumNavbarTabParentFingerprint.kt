package app.revanced.patches.spotify.navbar.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object AddPremiumNavbarTabParentFingerprint : MethodFingerprint(
     strings = listOf("com.samsung.android.samsungaccount.action.REQUEST_AUTHCODE")
)