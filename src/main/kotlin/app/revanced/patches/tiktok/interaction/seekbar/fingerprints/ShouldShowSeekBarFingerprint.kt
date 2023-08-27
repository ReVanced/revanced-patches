package app.revanced.patches.tiktok.interaction.seekbar.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object ShouldShowSeekBarFingerprint : MethodFingerprint(
    strings = listOf(
        "can not show seekbar, state: 1, not in resume"
    ),
)