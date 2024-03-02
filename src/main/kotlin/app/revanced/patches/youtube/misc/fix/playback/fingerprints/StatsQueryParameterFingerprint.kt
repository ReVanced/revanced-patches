package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object StatsQueryParameterFingerprint : MethodFingerprint(
    strings = listOf("adunit"),
)
