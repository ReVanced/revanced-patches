package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patches.youtube.layout.hide.shorts.reelPlayerRightCellButtonHeight
import app.revanced.util.patch.literalValueFingerprint

internal val createShortsButtonsFingerprint = literalValueFingerprint(
    literalSupplier = { reelPlayerRightCellButtonHeight },
) {
    returns("V")
}
