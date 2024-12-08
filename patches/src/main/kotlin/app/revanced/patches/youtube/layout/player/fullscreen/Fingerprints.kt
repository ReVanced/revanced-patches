package app.revanced.patches.youtube.layout.player.fullscreen

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal const val OPEN_VIDEOS_IN_PORTRAIT_FULLSCREEN_FEATURE_FLAG = 45666112L

internal val portraitFullscreenModeFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("L", "Lj\$/util/Optional;")
    literal {
        OPEN_VIDEOS_IN_PORTRAIT_FULLSCREEN_FEATURE_FLAG
    }
}
