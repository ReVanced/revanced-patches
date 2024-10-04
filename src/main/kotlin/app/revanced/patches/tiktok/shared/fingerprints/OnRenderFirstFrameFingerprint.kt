package app.revanced.patches.tiktok.shared.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object OnRenderFirstFrameFingerprint : MethodFingerprint(
    strings = listOf("method_enable_viewpager_preload_duration"),
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/BaseListFragmentPanel;")
    },
)
