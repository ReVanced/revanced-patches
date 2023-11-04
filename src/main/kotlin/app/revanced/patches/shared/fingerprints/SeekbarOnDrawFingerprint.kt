package app.revanced.patches.shared.fingerprints


import app.revanced.patcher.fingerprint.MethodFingerprint

object SeekbarOnDrawFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ -> methodDef.name == "onDraw" }
)