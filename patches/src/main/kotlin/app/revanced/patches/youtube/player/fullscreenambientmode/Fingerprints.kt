package app.revanced.patches.youtube.player.fullscreenambientmode

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val setFullScreenBackgroundColorFingerprint = fingerprint {
    returns("V")
    accessFlags(AccessFlags.PROTECTED, AccessFlags.FINAL)
    parameters("Z", "I", "I", "I", "I")
    custom { method, classDef ->
        classDef.type.endsWith("/YouTubePlayerViewNotForReflection;")
                && method.name == "onLayout"
    }
}
