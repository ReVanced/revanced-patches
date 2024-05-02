package app.revanced.patches.youtube.layout.buttons.navigation.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal const val ANDROID_AUTOMOTIVE_STRING = "Android Automotive"

internal val addCreateButtonViewFingerprint = methodFingerprint {
    strings("Android Wear", ANDROID_AUTOMOTIVE_STRING)
}
