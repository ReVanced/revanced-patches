package app.revanced.patches.tiktok.misc.settings

internal val BytecodePatchContext.addSettingsEntryMethod by gettingFirstMethodDeclaratively {
    custom { method, classDef ->
        classDef.endsWith("/SettingNewVersionFragment;") &&
            method.name == "initUnitManger"
    }
}

internal val BytecodePatchContext.adPersonalizationActivityOnCreateMethod by gettingFirstMethodDeclaratively {
    custom { method, classDef ->
        classDef.endsWith("/AdPersonalizationActivity;") &&
            method.name == "onCreate"
    }
}

internal val BytecodePatchContext.settingsEntryMethod by gettingFirstMethodDeclaratively {
    strings("pls pass item or extends the EventUnit")
}

internal val BytecodePatchContext.settingsEntryInfoMethod by gettingFirstMethodDeclaratively {
    strings(
        "ExposeItem(title=",
        ", icon=",
    )
}

internal val BytecodePatchContext.settingsStatusLoadMethod by gettingFirstMethodDeclaratively {
    custom { method, classDef ->
        classDef.endsWith("Lapp/revanced/extension/tiktok/settings/SettingsStatus;") &&
            method.name == "load"
    }
}
