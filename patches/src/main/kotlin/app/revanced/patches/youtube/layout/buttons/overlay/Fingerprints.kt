package app.revanced.patches.youtube.layout.buttons.overlay

import app.revanced.patcher.fingerprint

internal val mediaRouteButtonFingerprint by fingerprint {
    parameters("I")
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/MediaRouteButton;") && methodDef.name == "setVisibility"
    }
}
