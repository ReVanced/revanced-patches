package app.revanced.patches.warnwetter.misc.firebasegetcert.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.warnwetter.misc.firebasegetcert.annotations.FirebaseGetCertPatchCompatibility

@Name("registration-app-certificate-fingerprint")
@FirebaseGetCertPatchCompatibility
@Version("0.0.1")
object GetReqistrationCertFingerprint : MethodFingerprint(
    "Ljava/lang/String;",
    strings = listOf(
        "FirebaseRemoteConfig",
        "Could not get fingerprint hash for package: ",
        "No such package: "
    )
)