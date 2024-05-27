package app.revanced.patches.youtube.shared.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val seekbarOnDrawFingerprint = methodFingerprint {
    custom { methodDef, _ -> methodDef.name == "onDraw" }
}
