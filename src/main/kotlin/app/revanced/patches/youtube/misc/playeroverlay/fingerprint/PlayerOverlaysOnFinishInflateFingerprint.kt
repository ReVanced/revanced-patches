package app.revanced.patches.youtube.misc.playeroverlay.fingerprint


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object PlayerOverlaysOnFinishInflateFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("YouTubePlayerOverlaysLayout;") && methodDef.name == "onFinishInflate"
    }
)
