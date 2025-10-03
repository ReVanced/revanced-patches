package app.revanced.patches.music.layout.branding

import app.revanced.patcher.fingerprint
import app.revanced.patches.music.shared.YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE

internal val cairoSplashAnimationConfigFingerprint = fingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    custom { method, classDef ->
        method.name == "onCreate" && method.definingClass == YOUTUBE_MUSIC_MAIN_ACTIVITY_CLASS_TYPE
    }
}
