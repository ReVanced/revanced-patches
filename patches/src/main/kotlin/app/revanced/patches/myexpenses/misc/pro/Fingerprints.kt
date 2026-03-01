package app.revanced.patches.myexpenses.misc.pro

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.isEnabledMethod by gettingFirstMethodDeclaratively("feature", "feature.licenceStatus") {
    returnType("Z")
}