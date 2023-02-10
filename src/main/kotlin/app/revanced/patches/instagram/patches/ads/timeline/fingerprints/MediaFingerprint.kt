package app.revanced.patches.instagram.patches.ads.timeline.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object MediaFingerprint : MethodFingerprint(
    strings = listOf("is_paid_partnership", "story_ad_headline", "is_panorama")
)
