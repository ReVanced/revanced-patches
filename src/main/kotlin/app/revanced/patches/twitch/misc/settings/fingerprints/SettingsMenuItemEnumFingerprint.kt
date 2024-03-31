package app.revanced.patches.twitch.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object SettingsMenuItemEnumFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/SettingsMenuItem;") && methodDef.name == "<clinit>"
    }
)
