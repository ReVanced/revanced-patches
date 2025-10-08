package app.revanced.patches.instagram.hide.suggestionBlocks

import app.revanced.patcher.fingerprint

internal val FEED_ITEM_KEYS = listOf(
            "suggested_users",
            "suggested_top_accounts",
            "bloks_netego",
            "media_or_ad",
            "in_feed_survey",
            "stories_netego",
            "clips_netego",
            "FeedItem",
)

internal val feedItemParseFromJsonFingerprint = fingerprint {
        strings(*FEED_ITEM_KEYS.toTypedArray())
    }
