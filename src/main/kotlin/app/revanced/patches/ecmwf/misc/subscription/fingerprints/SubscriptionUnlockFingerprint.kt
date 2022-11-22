package app.revanced.patches.ecmwf.misc.subscription.fingerprints
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object SubscriptionUnlockFingerprint : MethodFingerprint(
    "Z",
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("MapsActivity;") && methodDef.name == "t0"
    }
)