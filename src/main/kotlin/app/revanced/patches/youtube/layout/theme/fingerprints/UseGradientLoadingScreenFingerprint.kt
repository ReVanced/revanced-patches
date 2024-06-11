package app.revanced.patches.youtube.layout.theme.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import app.revanced.patches.youtube.layout.theme.GRADIENT_LOADING_SCREEN_AB_CONSTANT
import app.revanced.util.literal

internal val useGradientLoadingScreenFingerprint = methodFingerprint {
    literal { GRADIENT_LOADING_SCREEN_AB_CONSTANT }
}
