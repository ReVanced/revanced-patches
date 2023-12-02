package app.revanced.patches.twitch.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object SettingsActivityOnCreateFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/SettingsActivity;") &&
                methodDef.name == "onCreate"
    }
)
