package app.revanced.patches.warnwetter.misc.firebasegetcert.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.warnwetter.misc.firebasegetcert.annotations.FirebaseGetCertPatchCompatibility

@Name("messaging-app-certificate-fingerprint")
@MatchingMethod(
    "Lcom/google/firebase/installations/remote/c;", "f"
)
@FirebaseGetCertPatchCompatibility
@Version("0.0.1")
object GetMessagingCertFingerprint : MethodFingerprint(
    "Ljava/lang/String;",
    null,
    null,
    null,
    listOf(
        "ContentValues",
        "Could not get fingerprint hash for package: ",
        "No such package: "
    )
)