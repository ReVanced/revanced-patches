package app.revanced.patches.ecmwf.misc.subscription.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.ecmwf.misc.subscription.annotations.SubscriptionUnlockCompatibility

@Name("subscription-unlock")
@MatchingMethod(
    "Lcom/garzotto/pflotsh/library_a/MapsActivity", "t0"
)
@SubscriptionUnlockCompatibility
@Version("0.0.1")
object SubscriptionUnlockFingerprint : MethodFingerprint(
    "Z",
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("MapsActivity;") && methodDef.name == "t0"
    }
)