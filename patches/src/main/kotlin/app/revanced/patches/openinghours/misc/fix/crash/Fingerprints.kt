package app.revanced.patches.openinghours.misc.fix.crash

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.definingClass
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.setPlaceMethod by gettingFirstMethodDeclaratively  {
    name("setPlace")
    definingClass("Lde/simon/openinghours/views/custom/PlaceCard;")
    returnType("V")
    parameterTypes("Lde/simon/openinghours/models/Place;")
}
