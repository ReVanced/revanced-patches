package app.revanced.patches.youtube.misc.dimensions.spoof

import app.revanced.patcher.addString
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.deviceDimensionsModelToStringMethod by gettingFirstMethodDeclaratively {
    returnType("L")
    instructions(
        "minh."(),
        ";maxh."(),
    )
}
