package app.revanced.patches.anytracker.misc.premium.fingerprints
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object CheckPremiumFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("BillingDataSource;") && methodDef.name == "isPurchased"
    }
)
