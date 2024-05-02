package app.revanced.patches.tiktok.interaction.speed.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val onRenderFirstFrameFingerprint = methodFingerprint {
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/BaseListFragmentPanel;") && methodDef.name == "onRenderFirstFrame"
    }
}
