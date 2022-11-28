package app.revanced.patches.twitch.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object SettingsMenuItemEnumFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/SettingsMenuItem;") && methodDef.name == "<clinit>"
    }
)
