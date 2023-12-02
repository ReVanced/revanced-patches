package app.revanced.patches.tiktok.ad.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object FeedItemListCloneFingerprint : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/FeedItemList;") && methodDef.name == "clone"
    }
)