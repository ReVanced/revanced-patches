package app.revanced.patches.tiktok.interaction.seekbar.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object SetSeekBarShowTypeFingerprint : MethodFingerprint(
    strings = listOf(
        "seekbar show type change, change to:"
    ),
)