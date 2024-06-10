package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.hide.shorts.reelPlayerRightCellButtonHeight
import app.revanced.util.literal

internal val createShortsButtonsFingerprint = methodFingerprint {
    returns("V")
    literal { reelPlayerRightCellButtonHeight }
}
