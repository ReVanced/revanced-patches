package app.revanced.patches.tiktok.interaction.cleardisplay

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.onClearDisplayEventMethod by gettingFirstMutableMethodDeclaratively {
    // Internally the feature is called "Clear mode".
    name("onClearModeEvent")
    definingClass { endsWith("/ClearModePanelComponent;") }
}
