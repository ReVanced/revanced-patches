
package app.revanced.patches.instagram.hide.search

import app.revanced.patcher.fingerprint

internal val exploreResponseJsonParserFingerprint = fingerprint{
    strings("sectional_items", "ExploreTopicalFeedResponse")
}
