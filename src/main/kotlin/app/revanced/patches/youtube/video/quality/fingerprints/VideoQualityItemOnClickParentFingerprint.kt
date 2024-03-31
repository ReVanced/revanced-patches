
package app.revanced.patches.youtube.video.quality.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object VideoQualityItemOnClickParentFingerprint : MethodFingerprint(
    "V",
    strings = listOf("VIDEO_QUALITIES_MENU_BOTTOM_SHEET_FRAGMENT")
)
