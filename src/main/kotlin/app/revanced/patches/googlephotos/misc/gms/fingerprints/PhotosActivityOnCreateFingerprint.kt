package app.revanced.patches.googlephotos.misc.gms.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object PhotosActivityOnCreateFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "onCreate" && classDef.endsWith("/HomeActivity;")
    },
)
