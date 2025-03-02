package app.revanced.patches.warnwetter.misc.firebasegetcert

import app.revanced.patcher.fingerprint

internal val getMessagingCertFingerprint = fingerprint {
    returns("Ljava/lang/String;")
    strings(
        "ContentValues",
        "Could not get fingerprint hash for package: ",
        "No such package: ",
    )
}

internal val getRegistrationCertFingerprint = fingerprint {
    returns("Ljava/lang/String;")
    strings(
        "FirebaseRemoteConfig",
        "Could not get fingerprint hash for package: ",
        "No such package: ",
    )
}
