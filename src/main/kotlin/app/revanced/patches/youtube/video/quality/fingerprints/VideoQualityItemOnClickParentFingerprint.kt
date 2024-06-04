
package app.revanced.patches.youtube.video.quality.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val videoQualityItemOnClickParentFingerprint = methodFingerprint {
    returns("V")
    strings("VIDEO_QUALITIES_MENU_BOTTOM_SHEET_FRAGMENT")
}
