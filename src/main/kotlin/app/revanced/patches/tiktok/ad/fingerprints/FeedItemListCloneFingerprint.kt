package app.revanced.patches.tiktok.ad.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.ad.annotations.TiktokAdsCompatibility

@Name("feed-item-clone-fingerprint")
@TiktokAdsCompatibility
@Version("0.0.1")
object FeedItemListCloneFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/FeedItemList;") && methodDef.name == "clone"
    }
)