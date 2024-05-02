package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val addSettingsEntryFingerprint = methodFingerprint {
    custom { methodDef, _ ->
        methodDef.definingClass.endsWith("/SettingNewVersionFragment;") &&
            methodDef.name == "initUnitManger"
    }
}
