package app.revanced.patches.tiktok.ad.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object FeedItemListCloneFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/FeedItemList;") && methodDef.name == "clone"
    }
)