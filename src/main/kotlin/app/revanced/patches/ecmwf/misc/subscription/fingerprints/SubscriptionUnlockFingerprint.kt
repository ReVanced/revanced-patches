package app.revanced.patches.ecmwf.misc.subscription.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.ecmwf.misc.subscription.annotations.SubscriptionUnlockCompatibility

@Name("subscription-unlock")
@SubscriptionUnlockCompatibility
@Version("0.0.1")
object SubscriptionUnlockFingerprint : MethodFingerprint(
    "Z",
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("MapsActivity;") && methodDef.name == "t0"
    }
)