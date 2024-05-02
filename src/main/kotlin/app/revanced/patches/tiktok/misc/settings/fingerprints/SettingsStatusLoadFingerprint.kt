package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val settingsStatusLoadFingerprint = methodFingerprint {
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("Lapp/revanced/integrations/tiktok/settings/SettingsStatus;") &&
            methodDef.name == "load"
    }
}
