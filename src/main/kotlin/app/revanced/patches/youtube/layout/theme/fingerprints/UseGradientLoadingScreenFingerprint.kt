package app.revanced.patches.youtube.layout.theme.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.theme.GRADIENT_LOADING_SCREEN_AB_CONSTANT

internal val useGradientLoadingScreenFingerprint = methodFingerprint(
    literal { GRADIENT_LOADING_SCREEN_AB_CONSTANT },
)
