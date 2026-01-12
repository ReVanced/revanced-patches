package app.revanced.patches.reddit.ad.comments

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.definingClass
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.hideCommentAdsMethod by gettingFirstMutableMethodDeclaratively {
    name("invokeSuspend")
    definingClass("LoadAdsCombinedCall"::contains)
}
