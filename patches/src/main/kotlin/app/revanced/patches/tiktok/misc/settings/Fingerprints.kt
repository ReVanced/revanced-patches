package app.revanced.patches.tiktok.misc.settings

import app.revanced.patcher.*
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

internal val BytecodePatchContext.settingsEntryInfoMethod by gettingFirstMethod("ExposeItem(title=", ", icon=")

internal val BytecodePatchContext.settingsStatusLoadMethod by gettingFirstMethodDeclaratively {
    name("load")
    definingClass { endsWith("Lapp/revanced/extension/tiktok/settings/SettingsStatus;") }
}
