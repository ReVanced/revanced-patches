package app.revanced.patches.tiktok.interaction.speed.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object SpeedControlParentFingerprint : MethodFingerprint(
    strings = listOf(
        "onStopTrackingTouch, hasTouchMove=",
        ", isCurVideoPaused: ",
        "already_shown_edge_speed_guide"
    )
)