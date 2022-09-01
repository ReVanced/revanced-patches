package app.revanced.patches.tiktok.ad.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.ad.annotations.TiktokAdsCompatibility

@Name("convert-help-v2-feeditemlist-fingerprint")
@MatchingMethod(
    "Lbeancopy/ConvertHelp;",
    "com${'$'}ss${'$'}ugc${'$'}aweme${'$'}proto${'$'}aweme_v2_feed_response${'$'}${'$'}com${'$'}ss${'$'}android${'$'}ugc${'$'}aweme${'$'}feed${'$'}model${'$'}FeedItemList",
)
@TiktokAdsCompatibility
@Version("0.0.1")
object ConvertHelpFeedItemListFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/ConvertHelp;") &&
                methodDef.name.endsWith("${'$'}FeedItemList")
    }
)