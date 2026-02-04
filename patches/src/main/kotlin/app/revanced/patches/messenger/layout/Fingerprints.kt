package app.revanced.patches.messenger.layout

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.isFacebookButtonEnabledMethod by gettingFirstMethodDeclaratively("FacebookButtonTabButtonImplementation") {
    parameterTypes()
    returnType("Z")
}
