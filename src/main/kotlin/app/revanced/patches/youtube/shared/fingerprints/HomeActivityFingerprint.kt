package app.revanced.patches.youtube.shared.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object HomeActivityFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "onCreate" && classDef.type.endsWith("Shell_HomeActivity;")
    },
)
