package app.revanced.patches.myexpenses.misc.pro

import app.revanced.patcher.BytecodePatchContextMethodMatching.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.isEnabledMethod by gettingFirstMutableMethodDeclaratively("feature", "feature.licenceStatus") {
    returnType("Z")
}