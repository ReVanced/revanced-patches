
package app.revanced.patches.instagram.hide.search

import app.revanced.patcher.fingerprint

internal val searchResponseJsonParserFingerprint = fingerprint{
    strings("sectional_items", "ExploreTopicalFeedResponse")
}
