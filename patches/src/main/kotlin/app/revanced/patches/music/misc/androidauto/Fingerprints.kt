package app.revanced.patches.music.misc.androidauto

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.checkCertificateMethod by gettingFirstMutableMethodDeclaratively(
    "X509",
    "Failed to get certificate" // Partial String match.
) {
    returnType("Z")
    parameterTypes("Ljava/lang/String;")
}