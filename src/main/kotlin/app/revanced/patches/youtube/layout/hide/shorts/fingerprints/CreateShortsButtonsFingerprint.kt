package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patches.youtube.layout.hide.shorts.HideShortsComponentsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint

internal object CreateShortsButtonsFingerprint : LiteralValueFingerprint(
    // YT 19.12.x moved this code inside another method, and each method has different parameters.
    returnType = "V",
    literalSupplier = { HideShortsComponentsResourcePatch.reelPlayerRightCellButtonHeight }
)