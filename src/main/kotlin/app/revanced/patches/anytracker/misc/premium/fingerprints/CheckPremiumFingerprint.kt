package app.revanced.patches.anytracker.misc.premium.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.anytracker.misc.premium.annotations.UnlockPremiumCompatibility

@Name("check-premium-fingerprint")
@UnlockPremiumCompatibility
@Version("0.0.1")
object CheckPremiumFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("BillingDataSource;") && methodDef.name == "isPurchased"
    }
)
