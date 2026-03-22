package app.revanced.patches.instagram.hide.suggestions

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.strings
import app.revanced.patcher.patch.BytecodePatchContext

internal val FEED_ITEM_KEYS_TO_BE_HIDDEN = arrayOf(
    "feed_item_type",
    "clips_netego",
    "stories_netego",
    "in_feed_survey",
    "bloks_netego",
    "suggested_igd_channels",
    "suggested_top_accounts",
    "suggested_users",
)

internal val BytecodePatchContext.feedItemParseFromJsonMethodMatch by composingFirstMethod {
    strings("feed_item_type")
}
