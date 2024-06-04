package app.revanced.patches.youtube.interaction.swipecontrols.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val swipeControlsHostActivityFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters()
    custom { _, classDef ->
        classDef.type == "Lapp/revanced/integrations/youtube/swipecontrols/SwipeControlsHostActivity;"
    }
}
