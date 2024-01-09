package app.revanced.patches.youtube.shared.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object SeekbarOnDrawFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ -> methodDef.name == "onDraw" }
)