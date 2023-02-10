package app.revanced.patches.instagram.patches.ads.timeline.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object MediaFingerprint : MethodFingerprint(
    strings = listOf("force_overlay", "Media#updateFields", "live_reels_metadata")
)
