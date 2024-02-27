package app.revanced.patches.youtube.utils.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object SeekbarOnDrawFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ -> methodDef.name == "onDraw" }
)