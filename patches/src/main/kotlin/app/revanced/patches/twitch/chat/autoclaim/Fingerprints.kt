package app.revanced.patches.twitch.chat.autoclaim

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.communityPointsButtonViewDelegateMethod by gettingFirstMutableMethodDeclaratively {
    name("showClaimAvailable")
    definingClass { endsWith("CommunityPointsButtonViewDelegate;") }
}
