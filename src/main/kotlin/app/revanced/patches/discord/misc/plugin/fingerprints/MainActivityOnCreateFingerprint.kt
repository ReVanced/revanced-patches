package app.revanced.patches.discord.misc.plugin.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object MainActivityOnCreateFingerprint : MethodFingerprint(
    customFingerprint = { method, classDef -> method.name == "onCreate" && classDef.endsWith("ReactActivity;") },
)
