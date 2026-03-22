package app.revanced.patches.amazonmusic.misc

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.type
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.getRuleMethod by gettingFirstMethodDeclaratively {
    type($$"Lcom/amazon/music/freetier/featuregating/FMPMFeatureGating$STATION_UNLIMITED_SKIPS;")
    name("getRule")
}
