package app.revanced.patches.youtube.interaction.swipecontrols

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val swipeControlsHostActivityFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters()
    custom { method, _ ->
        method.definingClass == "Lapp/revanced/extension/youtube/swipecontrols/SwipeControlsHostActivity;"
    }
}
