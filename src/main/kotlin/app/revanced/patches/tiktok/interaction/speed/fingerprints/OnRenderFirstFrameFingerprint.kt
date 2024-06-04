package app.revanced.patches.tiktok.interaction.speed.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val onRenderFirstFrameFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/BaseListFragmentPanel;") && methodDef.name == "onRenderFirstFrame"
    }
}
