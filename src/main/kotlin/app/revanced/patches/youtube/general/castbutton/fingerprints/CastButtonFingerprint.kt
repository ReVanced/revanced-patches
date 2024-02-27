package app.revanced.patches.youtube.general.castbutton.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object CastButtonFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/MediaRouteButton;")
                && methodDef.name == "setVisibility"
    }
)