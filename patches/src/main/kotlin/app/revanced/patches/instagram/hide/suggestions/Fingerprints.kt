package app.revanced.patches.instagram.hide.suggestions

import app.revanced.patcher.fingerprint

internal val FEED_ITEM_KEYS_TO_BE_HIDDEN = arrayOf(
    "clips_netego",
    "stories_netego",
    "in_feed_survey",
    "bloks_netego",
    "suggested_igd_channels",
    "suggested_top_accounts",
    "suggested_users",
)

internal val feedItemParseFromJsonFingerprint = fingerprint {
    strings(*FEED_ITEM_KEYS_TO_BE_HIDDEN, "FeedItem")
}
