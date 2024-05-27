package app.revanced.patches.youtube.layout.theme.fingerprints

import app.revanced.patches.youtube.layout.theme.ThemeBytecodePatch.GRADIENT_LOADING_SCREEN_AB_CONSTANT
import app.revanced.util.patch.literalValueFingerprint

internal val useGradientLoadingScreenFingerprint = literalValueFingerprint(
    literalSupplier = { GRADIENT_LOADING_SCREEN_AB_CONSTANT },
)
