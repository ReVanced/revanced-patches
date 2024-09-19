package app.revanced.patches.youtube.layout.hide.shorts.fingerprints

import app.revanced.patches.youtube.layout.hide.shorts.HideShortsComponentsResourcePatch
import app.revanced.util.patch.LiteralValueFingerprint

internal object ShortsSoundButtonSizeFingerprint : LiteralValueFingerprint(
    literalSupplier = { HideShortsComponentsResourcePatch.reelPlayerRightPivotV2Size }
)