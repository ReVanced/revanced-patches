package app.revanced.patches.instagram.misc.share

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.permalinkResponseJsonParserMethod by gettingFirstMutableMethodDeclaratively(
    "permalink", "PermalinkResponse",
) {
    name("parseFromJson")
}

internal val BytecodePatchContext.storyUrlResponseJsonParserMethod by gettingFirstMutableMethodDeclaratively(
    "story_item_to_share_url", "StoryItemUrlResponse",
) {
    name("parseFromJson")
}

internal val BytecodePatchContext.profileUrlResponseJsonParserMethod by gettingFirstMutableMethodDeclaratively(
    "profile_to_share_url", "ProfileUrlResponse",
) {
    name("parseFromJson")
}

internal val BytecodePatchContext.liveUrlResponseJsonParserMethod by gettingFirstMutableMethodDeclaratively(
    "live_to_share_url", "LiveItemLinkUrlResponse",
) {
    name("parseFromJson")
}
