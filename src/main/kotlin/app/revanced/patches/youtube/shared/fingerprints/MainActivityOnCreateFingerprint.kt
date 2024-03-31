package app.revanced.patches.youtube.shared.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object MainActivityOnCreateFingerprint : MethodFingerprint(
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "onCreate" &&
        (classDef.type.endsWith("MainActivity;")
                // Old versions of YouTube called this class "WatchWhileActivity" instead.
                || classDef.type.endsWith("WatchWhileActivity;"))
    }
)