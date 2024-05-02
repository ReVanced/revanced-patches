package app.revanced.patches.warnwetter.misc.firebasegetcert.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val getReqistrationCertFingerprint = methodFingerprint {
    returns("Ljava/lang/String;")
    strings(
        "FirebaseRemoteConfig",
        "Could not get fingerprint hash for package: ",
        "No such package: ",
    )
}
