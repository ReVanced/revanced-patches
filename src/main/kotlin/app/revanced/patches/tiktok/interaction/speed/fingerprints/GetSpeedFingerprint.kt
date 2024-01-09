package app.revanced.patches.tiktok.interaction.speed.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object GetSpeedFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/BaseListFragmentPanel;") && methodDef.name == "onFeedSpeedSelectedEvent"
    }
)