package app.revanced.patches.youtube.utils.microg.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object CastDynamiteModuleV2Fingerprint : MethodFingerprint(
    strings = listOf("Failed to load module via V2: ")
)