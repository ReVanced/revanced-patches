package app.revanced.patches.youtube.interaction.swipecontrols

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val swipeControlsHostActivityFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters()
    custom { method, _ ->
        method.definingClass == "Lapp/revanced/extension/youtube/swipecontrols/SwipeControlsHostActivity;"
    }
}

internal const val SWIPE_TO_SWITCH_VIDEO_FEATURE_FLAG = 45631116L

internal val swipeToSwitchVideoFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("L")
    literal {
        SWIPE_TO_SWITCH_VIDEO_FEATURE_FLAG
    }
}
