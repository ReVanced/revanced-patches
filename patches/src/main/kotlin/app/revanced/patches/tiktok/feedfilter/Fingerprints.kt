package app.revanced.patches.tiktok.feedfilter

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags


internal val feedItemListGetItemsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC)
    returns("Ljava/util/List;")
    custom { method, classDef ->
        classDef.endsWith("/FeedItemList;") && 
        method.name == "getItems" && 
        method.parameterTypes.isEmpty()
    }
}

internal val followFeedFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Lcom/ss/android/ugc/aweme/follow/presenter/FollowFeedList;")
    custom { method, _ ->
        method.parameterTypes.size == 2
    }
}