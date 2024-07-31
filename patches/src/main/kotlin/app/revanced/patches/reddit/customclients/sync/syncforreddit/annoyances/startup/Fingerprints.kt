package app.revanced.patches.reddit.customclients.sync.syncforreddit.annoyances.startup

import app.revanced.patcher.fingerprint

internal val mainActivityOnCreateFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.endsWith("MainActivity;") && method.name == "onCreate"
    }
}
