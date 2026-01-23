package app.revanced.patches.instagram.hide.explore

import app.revanced.patcher.firstMethodComposite
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.name

internal val exploreResponseJsonParserMethodMatch = firstMethodComposite("ExploreTopicalFeedResponse") {
    name("parseFromJson")
    instructions("sectional_items"())
}
