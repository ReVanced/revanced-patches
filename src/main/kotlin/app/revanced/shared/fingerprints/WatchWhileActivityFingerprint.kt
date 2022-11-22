package app.revanced.shared.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object WatchWhileActivityFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("WatchWhileActivity;") && methodDef.name == "<init>"
    }
)
