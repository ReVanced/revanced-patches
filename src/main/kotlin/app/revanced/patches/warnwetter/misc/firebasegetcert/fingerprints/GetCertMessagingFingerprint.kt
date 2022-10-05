package app.revanced.patches.warnwetter.misc.firebasegetcert.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.warnwetter.misc.firebasegetcert.annotations.FirebaseGetCertPatchCompatibility

@Name("messaging-app-certificate-fingerprint")
@FirebaseGetCertPatchCompatibility
@Version("0.0.1")
object GetMessagingCertFingerprint : MethodFingerprint(
    "Ljava/lang/String;",
    strings = listOf(
        "ContentValues",
        "Could not get fingerprint hash for package: ",
        "No such package: "
    )
)