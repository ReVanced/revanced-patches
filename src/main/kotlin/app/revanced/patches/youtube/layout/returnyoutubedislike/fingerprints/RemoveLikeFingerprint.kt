package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object RemoveLikeFingerprint : MethodFingerprint(
    "V",
    strings = listOf("like/removelike")
)