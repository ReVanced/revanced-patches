package app.revanced.patches.youtube.interaction.swipecontrols.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val swipeControlsHostActivityFingerprint = methodFingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
    parameters()
    custom { methodDef, _ ->
        methodDef.definingClass == "Lapp/revanced/integrations/youtube/swipecontrols/SwipeControlsHostActivity;"
    }
}
