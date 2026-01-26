package app.revanced.patches.instagram.misc.share

import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name

internal val permalinkResponseJsonParserMethodMatch = firstMethodComposite(
    "PermalinkResponse",
) {
    name("parseFromJson")
    instructions("permalink"())
}

internal val storyUrlResponseJsonParserMethodMatch = firstMethodComposite(
    "StoryItemUrlResponse",
) {
    name("parseFromJson")
    instructions("story_item_to_share_url"())
}

internal val profileUrlResponseJsonParserMethodMatch = firstMethodComposite(
    "ProfileUrlResponse",
) {
    name("parseFromJson")
    instructions("profile_to_share_url"())
}

internal val liveUrlResponseJsonParserMethodMatch = firstMethodComposite(
    "LiveItemLinkUrlResponse",
) {
    name("parseFromJson")
    instructions("live_to_share_url"())
}
