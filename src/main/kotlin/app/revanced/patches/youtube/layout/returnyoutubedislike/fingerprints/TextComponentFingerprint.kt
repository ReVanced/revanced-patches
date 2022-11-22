package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object TextComponentFingerprint : MethodFingerprint(
    strings = listOf("com.google.android.apps.youtube.music")
)