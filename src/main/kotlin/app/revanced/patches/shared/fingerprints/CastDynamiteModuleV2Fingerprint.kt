package app.revanced.patches.shared.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val castDynamiteModuleV2Fingerprint = methodFingerprint {
    strings("Failed to load module via V2: ")
}
