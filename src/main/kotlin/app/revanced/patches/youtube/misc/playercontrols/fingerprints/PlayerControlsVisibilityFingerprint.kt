package app.revanced.patches.youtube.misc.playercontrols.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.playercontrols.annotation.PlayerControlsCompatibility

@Name("player-controls-visibility-fingerprint")
@PlayerControlsCompatibility
@Version("0.0.1")
object PlayerControlsVisibilityFingerprint : MethodFingerprint(
    "V",
    parameters = listOf("Z", "Z"),
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("YouTubeControlsOverlay;")
    }
)