
package app.revanced.patches.instagram.hide.explore

import app.revanced.patcher.fingerprint

internal const val EXPLORE_KEY_TO_BE_HIDDEN = "sectional_items"

internal val exploreResponseJsonParserFingerprint = fingerprint {
    strings(EXPLORE_KEY_TO_BE_HIDDEN, "ExploreTopicalFeedResponse")
    custom { method, _ -> method.name == "parseFromJson" }
}
