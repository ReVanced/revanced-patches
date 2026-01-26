package app.revanced.patches.youtube.misc.dimensions.spoof

import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.deviceDimensionsModelToStringMethod by gettingFirstMutableMethodDeclaratively {
    returnType("L")
    instructions(
        "minh."(),
        ";maxh."(),
    )
}
