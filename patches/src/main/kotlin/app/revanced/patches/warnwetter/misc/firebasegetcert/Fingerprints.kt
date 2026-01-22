package app.revanced.patches.warnwetter.misc.firebasegetcert

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.getMessagingCertMethod by gettingFirstMutableMethodDeclaratively(
    "ContentValues",
    "Could not get fingerprint hash for package: ",
    "No such package: ",
) {
    returnType("Ljava/lang/String;")
}

internal val BytecodePatchContext.getRegistrationCertMethod by gettingFirstMutableMethodDeclaratively(
    "FirebaseRemoteConfig",
    "Could not get fingerprint hash for package: ",
    "No such package: ",
) {
    returnType("Ljava/lang/String;")
}
