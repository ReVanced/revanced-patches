package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object RenderBottomNavigationBarParentFingerprint : MethodFingerprint(
    parameters = listOf("I", "I", "L", "L", "J", "L"),
    strings = listOf("aa")
)