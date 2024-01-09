package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object LikeFingerprint : MethodFingerprint(
    "V",
    strings = listOf("like/like")
)