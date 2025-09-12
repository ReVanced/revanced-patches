package app.revanced.patches.tiktok.misc.settings

import app.revanced.patcher.fingerprint

internal val addSettingsEntryFingerprint by fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/SettingNewVersionFragment;") &&
            method.name == "initUnitManger"
    }
}

internal val adPersonalizationActivityOnCreateFingerprint by fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/AdPersonalizationActivity;") &&
            method.name == "onCreate"
    }
}

internal val settingsEntryFingerprint by fingerprint {
    strings("pls pass item or extends the EventUnit")
}

internal val settingsEntryInfoFingerprint by fingerprint {
    strings(
        "ExposeItem(title=",
        ", icon=",
    )
}

internal val settingsStatusLoadFingerprint by fingerprint {
    custom { method, classDef ->
        classDef.endsWith("Lapp/revanced/extension/tiktok/settings/SettingsStatus;") &&
            method.name == "load"
    }
}
