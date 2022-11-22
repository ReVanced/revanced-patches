package app.revanced.patches.warnwetter.misc.firebasegetcert.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object GetMessagingCertFingerprint : MethodFingerprint(
    "Ljava/lang/String;",
    strings = listOf(
        "ContentValues",
        "Could not get fingerprint hash for package: ",
        "No such package: "
    )
)