package app.revanced.patches.reddit.ad.comments

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.hideCommentAdsMethod by gettingFirstMethodDeclaratively {
    name("invokeSuspend")
    definingClass("LoadAdsCombinedCall")
}
