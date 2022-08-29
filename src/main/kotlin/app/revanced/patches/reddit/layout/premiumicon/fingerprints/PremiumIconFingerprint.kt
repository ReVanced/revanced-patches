package app.revanced.patches.reddit.layout.premiumicon.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patches.reddit.layout.premiumicon.annotations.PremiumIconCompatibility

@Name("premium-icon-fingerprint")
@MatchingMethod(
    "Lcom/reddit/domain/model/MyAccount;", "isPremiumSubscriber"
)
@PremiumIconCompatibility
@Version("0.0.1")
object PremiumIconFingerprint : MethodFingerprint(
    "Z",
    null,
    null,
    null,
    null,
    { methodDef ->
        methodDef.definingClass.endsWith("MyAccount;") && methodDef.name == "isPremiumSubscriber"
    }
)