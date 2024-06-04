package app.revanced.patches.twitch.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val settingsMenuItemEnumFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/SettingsMenuItem;") && methodDef.name == "<clinit>"
    }
}
