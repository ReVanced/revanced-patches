package app.revanced.patches.hexeditor.ad

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.primaryAdsMethod by gettingFirstMutableMethodDeclaratively {
    name("isAdsDisabled")
    definingClass("PreferencesHelper;"::endsWith)
}
