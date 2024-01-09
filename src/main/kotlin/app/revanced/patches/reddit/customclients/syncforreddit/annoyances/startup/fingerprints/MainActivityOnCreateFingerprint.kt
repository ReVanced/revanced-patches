package app.revanced.patches.reddit.customclients.syncforreddit.annoyances.startup.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object MainActivityOnCreateFingerprint : MethodFingerprint(
    customFingerprint = custom@{ method, classDef ->
        classDef.type.endsWith("MainActivity;") && method.name == "onCreate"
    }
)