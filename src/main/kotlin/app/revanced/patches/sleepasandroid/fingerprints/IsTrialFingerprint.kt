package app.revanced.patches.sleepasandroid.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object IsTrialFingerprint : MethodFingerprint(
    "Z",
    customFingerprint = { it.name == "isTrial" }
)