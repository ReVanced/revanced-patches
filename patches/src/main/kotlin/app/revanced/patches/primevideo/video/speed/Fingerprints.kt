package app.revanced.patches.primevideo.video.speed

import app.revanced.patcher.*
import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.playbackUserControlsInitializeMethod by gettingFirstMutableMethodDeclaratively {
    name("initialize")
    definingClass("Lcom/amazon/avod/playbackclient/activity/feature/PlaybackUserControlsFeature;")
    accessFlags(AccessFlags.PUBLIC)
    parameterTypes("Lcom/amazon/avod/playbackclient/PlaybackInitializationContext;")
    returnType("V")
}

internal val BytecodePatchContext.playbackUserControlsPrepareForPlaybackMethod by gettingFirstMutableMethodDeclaratively {
    name("prepareForPlayback")
    definingClass("Lcom/amazon/avod/playbackclient/activity/feature/PlaybackUserControlsFeature;")
    accessFlags(AccessFlags.PUBLIC)
    parameterTypes("Lcom/amazon/avod/playbackclient/PlaybackContext;")
    returnType("V")
}