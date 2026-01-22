package app.revanced.patches.messenger.layout

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.isFacebookButtonEnabledMethod by gettingFirstMutableMethodDeclaratively("FacebookButtonTabButtonImplementation") {
    parameterTypes()
    returnType("Z")
}
