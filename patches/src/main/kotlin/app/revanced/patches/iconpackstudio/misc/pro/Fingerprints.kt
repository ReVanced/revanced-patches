package app.revanced.patches.iconpackstudio.misc.pro

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.checkProMethod by gettingFirstMutableMethodDeclaratively {
    definingClass("IPSPurchaseRepository;"::endsWith)
    returnType("Z")
}