package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object TextComponentSpecParentFingerprint : MethodFingerprint(
    strings = listOf("TextComponentSpec: No converter for extension: ")
)