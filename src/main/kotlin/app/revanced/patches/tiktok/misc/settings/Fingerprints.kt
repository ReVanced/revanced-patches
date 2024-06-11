package app.revanced.patches.tiktok.misc.settings

import app.revanced.patcher.fingerprint.methodFingerprint

internal val addSettingsEntryFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/SettingNewVersionFragment;") &&
            methodDef.name == "initUnitManger"
    }
}

internal val adPersonalizationActivityOnCreateFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/AdPersonalizationActivity;") &&
            methodDef.name == "onCreate"
    }
}

internal val settingsEntryFingerprint = methodFingerprint {
    strings("pls pass item or extends the EventUnit")
}

internal val settingsEntryInfoFingerprint = methodFingerprint {
    strings(
        "ExposeItem(title=",
        ", icon=",
    )
}

internal val settingsStatusLoadFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("Lapp/revanced/integrations/tiktok/settings/SettingsStatus;") &&
            methodDef.name == "load"
    }
}
