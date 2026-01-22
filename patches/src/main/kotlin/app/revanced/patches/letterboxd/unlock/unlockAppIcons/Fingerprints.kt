package app.revanced.patches.letterboxd.unlock.unlockAppIcons

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getCanChangeAppIconMethod by gettingFirstMutableMethodDeclaratively {
    name("getCanChangeAppIcon")
    definingClass("SettingsAppIconFragment;"::endsWith)
}
