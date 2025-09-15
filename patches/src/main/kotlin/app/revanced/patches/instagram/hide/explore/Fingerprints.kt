
package app.revanced.patches.instagram.hide.explore

import app.revanced.patcher.fingerprint

internal val exploreResponseJsonParserFingerprint = fingerprint {
    strings("sectional_items", "ExploreTopicalFeedResponse")
    custom { method, _ -> method.name == "parseFromJson" }
}
