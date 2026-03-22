package app.revanced.patches.instagram.misc.signature

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.isValidSignatureMethodMethod by gettingFirstMethodDeclaratively {
    parameterTypes("L", "Z")
    returnType("Z")
    instructions(method("keySet"))
}
