package app.revanced.patches.instagram.misc.share

import app.revanced.patcher.fingerprint
import com.google.common.util.concurrent.Striped.custom

internal val permalinkResponseJsonParserFingerprint = fingerprint {
    strings("permalink", "PermalinkResponse")
    custom { method, _ -> method.name == "parseFromJson" }
}

internal val storyUrlResponseJsonParserFingerprint = fingerprint {
    strings("story_item_to_share_url", "StoryItemUrlResponse")
    custom { method, _ -> method.name == "parseFromJson" }
}

internal val profileUrlResponseJsonParserFingerprint = fingerprint {
    strings("profile_to_share_url", "ProfileUrlResponse")
    custom { method, _ -> method.name == "parseFromJson" }
}

internal val liveUrlResponseJsonParserFingerprint = fingerprint {
    strings("live_to_share_url", "LiveItemLinkUrlResponse")
    custom { method, _ -> method.name == "parseFromJson" }
}
