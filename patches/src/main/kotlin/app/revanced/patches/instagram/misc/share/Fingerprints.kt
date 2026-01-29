package app.revanced.patches.instagram.misc.share

import app.revanced.patcher.composingFirstMethod
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.permalinkResponseJsonParserMethodMatch by composingFirstMethod(
    "PermalinkResponse",
) {
    name("parseFromJson")
    instructions("permalink"())
}

internal val BytecodePatchContext.storyUrlResponseJsonParserMethodMatch by composingFirstMethod(
    "StoryItemUrlResponse",
) {
    name("parseFromJson")
    instructions("story_item_to_share_url"())
}

internal val BytecodePatchContext.profileUrlResponseJsonParserMethodMatch by composingFirstMethod(
    "ProfileUrlResponse",
) {
    name("parseFromJson")
    instructions("profile_to_share_url"())
}

internal val BytecodePatchContext.liveUrlResponseJsonParserMethodMatch by composingFirstMethod(
    "LiveItemLinkUrlResponse",
) {
    name("parseFromJson")
    instructions("live_to_share_url"())
}
