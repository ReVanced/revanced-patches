package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val dislikeFingerprint = methodFingerprint {
    returns("V")
    strings("like/dislike")
}
