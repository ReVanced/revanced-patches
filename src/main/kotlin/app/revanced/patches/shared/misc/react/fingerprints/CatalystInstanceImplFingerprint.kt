package app.revanced.patches.shared.misc.react.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal abstract class CatalystInstanceImplFingerprint(methodName: String) :
    MethodFingerprint(
        customFingerprint = { method, classDef ->
            method.name == methodName && classDef.endsWith("CatalystInstanceImpl;")
        },
    )
