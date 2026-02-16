package app.revanced.patches.warnwetter.misc.promocode

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.promoCodeUnlockMethod by gettingFirstMethodDeclaratively {
    name("isValid")
    definingClass("PromoTokenVerification;")
}
