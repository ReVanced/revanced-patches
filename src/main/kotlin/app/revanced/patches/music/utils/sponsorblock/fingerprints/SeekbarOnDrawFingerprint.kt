package app.revanced.patches.music.utils.sponsorblock.bytecode.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object SeekbarOnDrawFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ -> methodDef.name == "onDraw" }
)