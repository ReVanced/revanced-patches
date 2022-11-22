package app.revanced.patches.warnwetter.misc.firebasegetcert.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object GetReqistrationCertFingerprint : MethodFingerprint(
    "Ljava/lang/String;",
    strings = listOf(
        "FirebaseRemoteConfig",
        "Could not get fingerprint hash for package: ",
        "No such package: "
    )
)