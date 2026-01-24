package app.revanced.patches.warnwetter.misc.promocode

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.promoCodeUnlockMethod by gettingFirstMutableMethodDeclaratively {
    name("isValid")
    definingClass { endsWith("PromoTokenVerification;") }
}
