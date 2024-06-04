package app.revanced.patches.twitter.misc.hook.json.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object LoganSquareFingerprint : MethodFingerprint(
    customFingerprint = { _, classDef -> classDef.endsWith("LoganSquare;") }
)