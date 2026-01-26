package app.revanced.patches.tiktok.feedfilter

import app.revanced.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val feedApiLIZIZFingerprint = fingerprint {
    // TikTok 43.6.2: Lcom/ss/android/ugc/aweme/feed/api/FeedApi;->LIZIZ(LX/0Qft;)Lcom/ss/android/ugc/aweme/feed/model/FeedItemList;
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Lcom/ss/android/ugc/aweme/feed/model/FeedItemList;")
    parameters("LX/0Qft;")
    custom { method, classDef ->
        classDef.endsWith("/FeedApi;") && method.name == "LIZIZ"
    }
}

internal val feedItemListGetItemsFingerprint = fingerprint {
    // TikTok 43.6.2: Lcom/ss/android/ugc/aweme/feed/model/FeedItemList;->getItems()Ljava/util/List;
    accessFlags(AccessFlags.PUBLIC)
    returns("Ljava/util/List;")
    custom { method, classDef ->
        classDef.endsWith("/FeedItemList;") && method.name == "getItems" && method.parameterTypes.isEmpty()
    }
}

internal val followFeedFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Lcom/ss/android/ugc/aweme/follow/presenter/FollowFeedList;")
    custom { method, _ ->
        // TikTok 43.6.2: LX/*;->LIZ(LX/*;LX/*;)Lcom/ss/android/ugc/aweme/follow/presenter/FollowFeedList;
        method.parameterTypes.size == 2
    }
}
