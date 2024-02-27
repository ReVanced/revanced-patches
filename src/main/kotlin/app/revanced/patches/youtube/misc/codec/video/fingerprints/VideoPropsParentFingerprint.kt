package app.revanced.patches.youtube.misc.codec.video.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object VideoPropsParentFingerprint : MethodFingerprint(
    returnType = "V",
    strings = listOf("Android Wear")
)
