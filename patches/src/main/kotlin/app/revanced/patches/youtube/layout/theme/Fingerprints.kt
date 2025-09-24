package app.revanced.patches.youtube.layout.theme

import app.revanced.patcher.fingerprint
import app.revanced.patches.youtube.shared.YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE
import app.revanced.util.literal

internal const val GRADIENT_LOADING_SCREEN_AB_CONSTANT = 45412406L

internal val useGradientLoadingScreenFingerprint = fingerprint {
    literal { GRADIENT_LOADING_SCREEN_AB_CONSTANT }
}

internal const val SPLASH_SCREEN_STYLE_FEATURE_FLAG = 269032877L

internal val splashScreenStyleFingerprint = fingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    literal { SPLASH_SCREEN_STYLE_FEATURE_FLAG }
    custom { method, classDef ->
        method.name == "onCreate" && classDef.type == YOUTUBE_MAIN_ACTIVITY_CLASS_TYPE
    }
}
