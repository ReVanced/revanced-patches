package app.revanced.patches.youtube.layout.tabletminiplayer.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object MiniPlayerOverrideParentFingerprint : MethodFingerprint(
    strings = listOf("Possible Context wrapper loop - chain of wrappers larger than 10000")
)