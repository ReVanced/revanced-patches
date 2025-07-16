package app.revanced.patches.primevideo.video.speed

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val playbackUserControlsInitializeFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    parameters("Lcom/amazon/avod/playbackclient/PlaybackInitializationContext;")
    returns("V")
    custom { method, classDef ->
        method.name == "initialize" && classDef.type == "Lcom/amazon/avod/playbackclient/activity/feature/PlaybackUserControlsFeature;"
    }
}

internal val playbackUserControlsPrepareForPlaybackFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    parameters("Lcom/amazon/avod/playbackclient/PlaybackContext;")
    returns("V")
    custom { method, classDef ->
        method.name == "prepareForPlayback" && 
        classDef.type == "Lcom/amazon/avod/playbackclient/activity/feature/PlaybackUserControlsFeature;"
    }
}