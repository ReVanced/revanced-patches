package app.revanced.patches.tiktok.misc.settings

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.addSettingsEntryMethod by gettingFirstMutableMethodDeclaratively {
    name("initUnitManger")
    definingClass { endsWith("/SettingNewVersionFragment;") }
}

internal val BytecodePatchContext.adPersonalizationActivityOnCreateMethod by gettingFirstMutableMethodDeclaratively {
    name("onCreate")
    definingClass { endsWith("/AdPersonalizationActivity;") }
}

internal val BytecodePatchContext.settingsEntryMethod by gettingFirstMethodDeclaratively(
    "pls pass item or extends the EventUnit",
)

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
