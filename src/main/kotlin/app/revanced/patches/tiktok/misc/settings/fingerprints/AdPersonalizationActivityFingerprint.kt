package app.revanced.patches.tiktok.misc.settings.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.misc.settings.annotations.TikTokSettingsCompatibility

@Name("ad-personalization-activity-fingerprint")
@MatchingMethod(
    "Lcom/bytedance/ies/ugc/aweme/commercialize/compliance/personalization/AdPersonalizationActivity;",
    "onCreate"
)
@TikTokSettingsCompatibility
@Version("0.0.1")
object AdPersonalizationActivityFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/AdPersonalizationActivity;") &&
                methodDef.name == "onCreate"
    }
)