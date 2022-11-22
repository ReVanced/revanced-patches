package app.revanced.patches.youtube.layout.returnyoutubedislike.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.method.annotation.FuzzyPatternScanMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import org.jf.dexlib2.AccessFlags


@FuzzyPatternScanMethod(2)
object LikeFingerprint : MethodFingerprint(
    "V",
    AccessFlags.PROTECTED or AccessFlags.CONSTRUCTOR,
     strings = listOf("like/like")
)