package app.revanced.patches.messenger.layout

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke

internal val BytecodePatchContext.isFacebookButtonEnabledMethod by gettingFirstMethodDeclaratively {
    parameterTypes()
    returnType("Z")
    instructions("FacebookButtonTabButtonImplementation"(String::contains))
}
