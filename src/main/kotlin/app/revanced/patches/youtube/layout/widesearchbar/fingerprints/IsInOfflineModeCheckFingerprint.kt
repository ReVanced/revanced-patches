package app.revanced.patches.youtube.layout.widesearchbar.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object IsInOfflineModeCheckFingerprint : MethodFingerprint(
    "L",
    strings = listOf("bundle_is_in_offline_mode")
)