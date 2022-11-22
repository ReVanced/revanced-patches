package app.revanced.patches.youtube.interaction.swipecontrols.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object SwipeControlsHostActivityFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass == "Lapp/revanced/integrations/swipecontrols/SwipeControlsHostActivity;" && methodDef.name == "<init>"
    }
)
