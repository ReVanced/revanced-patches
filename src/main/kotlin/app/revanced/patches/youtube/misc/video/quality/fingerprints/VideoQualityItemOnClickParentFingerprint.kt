
package app.revanced.patches.youtube.misc.video.quality.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object VideoQualityItemOnClickParentFingerprint : MethodFingerprint(
    "V",
    strings = listOf("VIDEO_QUALITIES_MENU_BOTTOM_SHEET_FRAGMENT")
)