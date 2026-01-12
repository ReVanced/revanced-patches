package app.revanced.patches.primevideo.video.speed

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.accessFlags
import app.revanced.patcher.definingClass
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import com.android.tools.smali.dexlib2.AccessFlags

internal val BytecodePatchContext.playbackUserControlsInitializeMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC)
    parameterTypes("Lcom/amazon/avod/playbackclient/PlaybackInitializationContext;")
    returnType("V")
    name("initialize")
    definingClass("Lcom/amazon/avod/playbackclient/activity/feature/PlaybackUserControlsFeature;")
}

internal val BytecodePatchContext.playbackUserControlsPrepareForPlaybackMethod by gettingFirstMutableMethodDeclaratively {
    accessFlags(AccessFlags.PUBLIC)
    parameterTypes("Lcom/amazon/avod/playbackclient/PlaybackContext;")
    returnType("V")
    name("prepareForPlayback")
    definingClass("Lcom/amazon/avod/playbackclient/activity/feature/PlaybackUserControlsFeature;")
}