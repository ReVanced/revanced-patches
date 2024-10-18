package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object LegacyRenderBottomNavigationBarParentFingerprint : MethodFingerprint(
    parameters = listOf(
        "I",
        "I",
        "L",
        "L",
        "J",
        "L",
    ),
    strings = listOf("aa")
)