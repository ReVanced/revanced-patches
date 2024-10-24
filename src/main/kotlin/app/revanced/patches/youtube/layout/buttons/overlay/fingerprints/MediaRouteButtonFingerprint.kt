package app.revanced.patches.youtube.layout.buttons.overlay.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object MediaRouteButtonFingerprint : MethodFingerprint (
    parameters = listOf("I"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/MediaRouteButton;") && methodDef.name == "setVisibility"
    }
)