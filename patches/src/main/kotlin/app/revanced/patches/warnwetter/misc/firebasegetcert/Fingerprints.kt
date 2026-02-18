package app.revanced.patches.warnwetter.misc.firebasegetcert

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.getMessagingCertMethod by gettingFirstMethodDeclaratively(
    "ContentValues",
    "Could not get fingerprint hash for package: ",
    "No such package: ",
) {
    returnType("Ljava/lang/String;")
}

internal val BytecodePatchContext.getRegistrationCertMethod by gettingFirstMethodDeclaratively(
    "FirebaseRemoteConfig",
    "Could not get fingerprint hash for package: ",
    "No such package: ",
) {
    returnType("Ljava/lang/String;")
}
