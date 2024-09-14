package app.revanced.patches.youtube.misc.fix.playback.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

@Deprecated("Fingerprint is obsolete and will be deleted soon")
internal object StatsQueryParameterFingerprint : MethodFingerprint(
    strings = listOf("adunit"),
)
