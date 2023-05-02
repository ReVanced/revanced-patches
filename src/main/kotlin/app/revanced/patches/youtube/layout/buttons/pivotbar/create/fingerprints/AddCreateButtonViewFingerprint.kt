package app.revanced.patches.youtube.layout.buttons.pivotbar.create.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

internal const val ANDROID_AUTOMOTIVE_STRING = "Android Automotive"

object AddCreateButtonViewFingerprint : MethodFingerprint(
    strings = listOf(
        "Android Wear",
        ANDROID_AUTOMOTIVE_STRING,
    )
)