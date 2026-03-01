package app.revanced.patches.nothingx.misc.logk1token

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

/**
 * Fingerprint for the Application onCreate method.
 * This is used to trigger scanning for existing log files on app startup.
 */
internal val BytecodePatchContext.applicationOnCreateMethod by gettingFirstMethodDeclaratively {
    name("onCreate")
    definingClass("BaseApplication;")
    returnType("V")
    parameterTypes()
}
