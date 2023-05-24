package app.revanced.patches.youtube.layout.hide.shorts.bytecode.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object RenderBottomNavigationBarParentFingerprint : MethodFingerprint(
    parameters = listOf("I", "I", "L", "L", "J", "L"),
    strings = listOf("aa")
)