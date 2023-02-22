package app.revanced.patches.twitter.misc.hook.json.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object LoganSquareFingerprint : MethodFingerprint(
    customFingerprint = { methodDef -> methodDef.definingClass.endsWith("LoganSquare;") }
)