package app.revanced.patches.warnwetter.misc.firebasegetcert.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val getMessagingCertFingerprint = methodFingerprint {
    returns("Ljava/lang/String;")
    strings(
        "ContentValues",
        "Could not get fingerprint hash for package: ",
        "No such package: ",
    )
}
