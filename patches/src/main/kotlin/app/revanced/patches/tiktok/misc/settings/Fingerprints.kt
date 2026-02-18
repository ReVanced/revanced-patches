package app.revanced.patches.tiktok.misc.settings

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.addSettingsEntryMethod by gettingFirstMethodDeclaratively {
    name("initUnitManger")
    definingClass("/SettingNewVersionFragment;")
}

internal val BytecodePatchContext.adPersonalizationActivityOnCreateMethod by gettingFirstMethodDeclaratively {
    name("onCreate")
    definingClass("/AdPersonalizationActivity;")
}

internal val BytecodePatchContext.settingsEntryMethod by gettingFirstImmutableMethodDeclaratively(
    "pls pass item or extends the EventUnit",
)

internal val BytecodePatchContext.settingsEntryInfoMethod by gettingFirstImmutableMethod("ExposeItem(title=", ", icon=")

internal val BytecodePatchContext.settingsStatusLoadMethod by gettingFirstMethodDeclaratively {
    name("load")
    definingClass("Lapp/revanced/extension/tiktok/settings/SettingsStatus;")
}
