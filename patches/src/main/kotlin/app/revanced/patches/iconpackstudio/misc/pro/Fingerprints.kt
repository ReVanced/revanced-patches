package app.revanced.patches.iconpackstudio.misc.pro

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.checkProMethod by gettingFirstMethodDeclaratively {
    definingClass("IPSPurchaseRepository;")
    returnType("Z")
}
