package app.revanced.patches.tiktok.interaction.cleardisplay.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object OnRenderFirstFrameFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/BaseListFragmentPanel;") && methodDef.name == "onRenderFirstFrame"
    }
)