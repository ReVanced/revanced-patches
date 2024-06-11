package app.revanced.patches.youtube.interaction.swipecontrols

import com.android.tools.smali.dexlib2.AccessFlags
import app.revanced.patcher.fingerprint.methodFingerprint

internal val swipeControlsHostActivityFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters()
    custom { _, classDef ->
        classDef.type == "Lapp/revanced/integrations/youtube/swipecontrols/SwipeControlsHostActivity;"
    }
}
