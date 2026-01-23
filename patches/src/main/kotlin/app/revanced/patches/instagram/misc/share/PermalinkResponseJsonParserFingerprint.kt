package app.revanced.patches.instagram.misc.share

import com.google.common.util.concurrent.Striped.custom

internal val BytecodePatchContext.permalinkResponseJsonParserMethod by gettingFirstMethodDeclaratively {
    strings("permalink", "PermalinkResponse")
    custom { method, _ -> method.name == "parseFromJson" }
}

internal val BytecodePatchContext.storyUrlResponseJsonParserMethod by gettingFirstMethodDeclaratively {
    strings("story_item_to_share_url", "StoryItemUrlResponse")
    custom { method, _ -> method.name == "parseFromJson" }
}

internal val BytecodePatchContext.profileUrlResponseJsonParserMethod by gettingFirstMethodDeclaratively {
    strings("profile_to_share_url", "ProfileUrlResponse")
    custom { method, _ -> method.name == "parseFromJson" }
}

internal val BytecodePatchContext.liveUrlResponseJsonParserMethod by gettingFirstMethodDeclaratively {
    strings("live_to_share_url", "LiveItemLinkUrlResponse")
    custom { method, _ -> method.name == "parseFromJson" }
}
