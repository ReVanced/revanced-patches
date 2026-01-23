package app.revanced.patches.youtube.layout.hide.relatedvideooverlay

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType
import app.revanced.patches.shared.misc.mapping.ResourceType

internal val BytecodePatchContext.relatedEndScreenResultsParentMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    instructions(
        ResourceType.LAYOUT("app_related_endscreen_results"),
    )
}

internal val BytecodePatchContext.relatedEndScreenResultsMethod by gettingFirstMethodDeclaratively {
    returnType("V")
    parameterTypes(
        "I",
        "Z",
        "I",
    )
}
