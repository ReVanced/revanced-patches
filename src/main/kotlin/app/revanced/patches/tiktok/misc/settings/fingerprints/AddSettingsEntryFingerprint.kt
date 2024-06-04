package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val addSettingsEntryFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/SettingNewVersionFragment;") &&
            methodDef.name == "initUnitManger"
    }
}
