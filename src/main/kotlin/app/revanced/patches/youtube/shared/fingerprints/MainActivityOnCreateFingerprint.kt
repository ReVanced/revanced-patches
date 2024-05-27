package app.revanced.patches.youtube.shared.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val mainActivityOnCreateFingerprint = methodFingerprint {
    returns("V")
    parameters("Landroid/os/Bundle;")
    custom { methodDef, classDef ->
        methodDef.name == "onCreate" &&
            (
                classDef.endsWith("MainActivity;") ||
                    // Old versions of YouTube called this class "WatchWhileActivity" instead.
                    classDef.endsWith("WatchWhileActivity;")
                )
    }
}
