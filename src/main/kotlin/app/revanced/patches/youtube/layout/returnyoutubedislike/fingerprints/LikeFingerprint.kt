package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint


@FuzzyPatternScanMethod(2)
object LikeFingerprint : MethodFingerprint(
    "V",
     strings = listOf("like/like")
)