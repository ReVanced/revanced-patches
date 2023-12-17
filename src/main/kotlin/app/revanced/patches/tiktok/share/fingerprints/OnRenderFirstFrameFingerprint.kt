package app.revanced.patches.tiktok.share.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object OnRenderFirstFrameFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/BaseListFragmentPanel;") &&
                methodDef.name == "onRenderFirstFrame"
    }
)