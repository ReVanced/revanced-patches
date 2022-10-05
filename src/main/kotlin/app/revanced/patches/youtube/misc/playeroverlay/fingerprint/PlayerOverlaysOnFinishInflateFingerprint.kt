package app.revanced.patches.youtube.misc.playeroverlay.fingerprint

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.playeroverlay.annotation.PlayerOverlaysHookCompatibility

@Name("player-overlays-onFinishInflate-fingerprint")
@PlayerOverlaysHookCompatibility
@Version("0.0.1")
object PlayerOverlaysOnFinishInflateFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("YouTubePlayerOverlaysLayout;") && methodDef.name == "onFinishInflate"
    }
)
