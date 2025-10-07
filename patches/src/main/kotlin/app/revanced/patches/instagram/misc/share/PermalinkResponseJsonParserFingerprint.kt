package app.revanced.patches.instagram.misc.share

import app.revanced.patcher.fingerprint
import com.google.common.util.concurrent.Striped.custom

internal val permalinkResponseJsonParserFingerprint by fingerprint {
    strings("permalink", "PermalinkResponse")
    custom { method, _ -> method.name == "parseFromJson" }
}

internal val storyUrlResponseJsonParserFingerprint by fingerprint {
    strings("story_item_to_share_url", "StoryItemUrlResponse")
    custom { method, _ -> method.name == "parseFromJson" }
}

internal val profileUrlResponseJsonParserFingerprint by fingerprint {
    strings("profile_to_share_url", "ProfileUrlResponse")
    custom { method, _ -> method.name == "parseFromJson" }
}

internal val liveUrlResponseJsonParserFingerprint by fingerprint {
    strings("live_to_share_url", "LiveItemLinkUrlResponse")
    custom { method, _ -> method.name == "parseFromJson" }
}
