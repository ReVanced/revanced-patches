package app.revanced.patches.googlenews.misc.gms.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object MagazinesActivityOnCreateFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "onCreate" && classDef.endsWith("/StartActivity;")
    },
)
