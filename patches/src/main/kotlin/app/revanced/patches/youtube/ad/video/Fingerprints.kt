package app.revanced.patches.youtube.ad.video

import app.revanced.patcher.fingerprint

internal val loadVideoAdsFingerprint = fingerprint {
    strings(
        "TriggerBundle doesn't have the required metadata specified by the trigger ",
        "Ping migration no associated ping bindings for activated trigger: ",
    )
}
