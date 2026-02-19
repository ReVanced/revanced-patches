package app.revanced.patches.twitch.chat.autoclaim

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.communityPointsButtonViewDelegateMethod by gettingFirstMethodDeclaratively {
    name("showClaimAvailable")
    definingClass { endsWith("CommunityPointsButtonViewDelegate;") }
}
