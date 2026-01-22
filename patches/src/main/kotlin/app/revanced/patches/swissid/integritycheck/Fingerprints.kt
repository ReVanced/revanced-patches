package app.revanced.patches.swissid.integritycheck

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.checkIntegrityMethod by gettingFirstMutableMethodDeclaratively("it", "result") {
    returnType("V")
    parameterTypes("Lcom/swisssign/deviceintegrity/model/DeviceIntegrityResult;")
}
