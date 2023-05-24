package app.revanced.patches.shared.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object SeekbarOnDrawFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ -> methodDef.name == "onDraw" }
)