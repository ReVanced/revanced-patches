package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object LikeFingerprint : MethodFingerprint(
    "V",
    strings = listOf("like/like")
)