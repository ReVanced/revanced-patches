package app.revanced.patches.instagram.hide.suggestions

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.unorderedAllOf

internal val FEED_ITEM_KEYS_TO_BE_HIDDEN = arrayOf(
    "clips_netego",
    "stories_netego",
    "in_feed_survey",
    "bloks_netego",
    "suggested_igd_channels",
    "suggested_top_accounts",
    "suggested_users",
)

internal val BytecodePatchContext.feedItemParseFromJsonMethodMatch by composingFirstMethod("FeedItem") {
    instructions(predicates = unorderedAllOf(predicates = FEED_ITEM_KEYS_TO_BE_HIDDEN.map { it() }.toTypedArray()))
}
