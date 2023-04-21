package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object DislikeFingerprint : MethodFingerprint(
    "V",
    strings = listOf("like/dislike")
)