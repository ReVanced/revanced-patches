package app.revanced.patches.instagram.hide.suggestionBlocks

import app.revanced.patcher.fingerprint

internal const val FEED_ITEM_SUGGESTED_USERS_BLOCK_KEY = "suggested_users"
internal const val FEED_ITEM_SUGGESTED_TOP_ACCOUNTS_BLOCK_KEY = "suggested_top_accounts"
internal const val FEED_ITEM_SUGGESTED_THREADS_BLOCK_KEY = "bloks_netego"
internal const val FEED_ITEM_SPONSORED_POST_BLOCK_KEY = "media_or_ad"
internal const val FEED_ITEM_SUGGESTED_SURVEY_BLOCK_KEY = "in_feed_survey"
internal const val FEED_ITEM_SUGGESTED_STORY_BLOCK_KEY = "stories_netego"
internal const val FEED_ITEM_SUGGESTED_REELS_BLOCK_KEY = "clips_netego"

internal val feedItemParseFromJsonFingerprint = fingerprint {
        strings(
            FEED_ITEM_SUGGESTED_USERS_BLOCK_KEY,
            FEED_ITEM_SUGGESTED_TOP_ACCOUNTS_BLOCK_KEY,
            FEED_ITEM_SUGGESTED_THREADS_BLOCK_KEY,
            FEED_ITEM_SPONSORED_POST_BLOCK_KEY,
            FEED_ITEM_SUGGESTED_SURVEY_BLOCK_KEY,
            FEED_ITEM_SUGGESTED_STORY_BLOCK_KEY,
            FEED_ITEM_SUGGESTED_REELS_BLOCK_KEY,
            "FeedItem"
        )
    }
