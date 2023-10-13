package app.revanced.patches.youtube.layout.theme.fingerprints

import app.revanced.patches.youtube.layout.theme.ThemeBytecodePatch.GRADIENT_LOADING_SCREEN_AB_CONSTANT
import app.revanced.util.patch.LiteralValueFingerprint

object UseGradientLoadingScreenFingerprint : LiteralValueFingerprint(
    literalSupplier = { GRADIENT_LOADING_SCREEN_AB_CONSTANT }
)