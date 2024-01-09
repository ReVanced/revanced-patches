package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object DislikeFingerprint : MethodFingerprint(
    "V",
    strings = listOf("like/dislike")
)