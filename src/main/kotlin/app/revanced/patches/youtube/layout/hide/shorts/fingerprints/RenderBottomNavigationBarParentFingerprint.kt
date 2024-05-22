package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val renderBottomNavigationBarParentFingerprint = methodFingerprint {
    parameters("I", "I", "L", "L", "J", "L")
    strings("aa")
}
