package app.revanced.patches.youtube.interaction.swipecontrols

import app.revanced.patcher.fingerprint
import app.revanced.util.literal
import com.android.tools.smali.dexlib2.AccessFlags

internal val swipeControlsHostActivityFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters()
    custom { method, _ ->
        method.definingClass == EXTENSION_CLASS_DESCRIPTOR
    }
}

internal const val SWIPE_CHANGE_VIDEO_FEATURE_FLAG = 45631116L

internal val swipeChangeVideoFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters("L")
    literal {
        SWIPE_CHANGE_VIDEO_FEATURE_FLAG
    }
}
