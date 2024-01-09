package app.revanced.patches.youtube.misc.gms.fingerprints


import app.revanced.patcher.fingerprint.MethodFingerprint

internal object CastDynamiteModuleV2Fingerprint : MethodFingerprint(
    strings = listOf("Failed to load module via V2: ")
)