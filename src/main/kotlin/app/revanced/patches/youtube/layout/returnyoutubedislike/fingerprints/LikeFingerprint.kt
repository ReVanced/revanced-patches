package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val likeFingerprint = methodFingerprint {
    returns("V")
    strings("like/like")
}
