package app.revanced.patches.tiktok.ad.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.ad.annotations.TiktokAdsCompatibility

@Name("convert-help-v2-feeditemlist-fingerprint")
@TiktokAdsCompatibility
@Version("0.0.1")
object ConvertHelpFeedItemListFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/ConvertHelp;") &&
                methodDef.name.endsWith("${'$'}FeedItemList")
    }
)