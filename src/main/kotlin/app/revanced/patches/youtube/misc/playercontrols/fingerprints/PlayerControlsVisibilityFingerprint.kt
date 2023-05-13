package app.revanced.patches.youtube.misc.playercontrols.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object PlayerControlsVisibilityFingerprint : MethodFingerprint(
    "V",
    parameters = listOf("Z", "Z"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("YouTubeControlsOverlay;")
    }
)