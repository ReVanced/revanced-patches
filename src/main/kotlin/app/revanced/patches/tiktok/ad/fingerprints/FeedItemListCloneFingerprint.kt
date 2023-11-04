package app.revanced.patches.tiktok.ad.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

object FeedItemListCloneFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/FeedItemList;") && methodDef.name == "clone"
    }
)