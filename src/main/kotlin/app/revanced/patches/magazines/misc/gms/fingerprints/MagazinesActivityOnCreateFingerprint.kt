package app.revanced.patches.magazines.misc.gms.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object MagazinesActivityOnCreateFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, classDef ->
       classDef.type.endsWith("/StartActivity;") && methodDef.name == "onCreate"
    }
)
