package app.revanced.patches.hexeditor.ad

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.primaryAdsMethod by gettingFirstMethodDeclaratively {
    name("isAdsDisabled")
    definingClass { endsWith("PreferencesHelper;") }
}
