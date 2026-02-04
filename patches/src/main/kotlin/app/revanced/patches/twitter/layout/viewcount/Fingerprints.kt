package app.revanced.patches.twitter.layout.viewcount

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.viewCountsEnabledMethod by gettingFirstMethodDeclaratively("view_counts_public_visibility_enabled") {
    returnType("Z")

}
