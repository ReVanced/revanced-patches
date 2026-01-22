package app.revanced.patches.disneyplus

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType


internal val BytecodePatchContext.insertionGetPointsMethod by gettingFirstMutableMethodDeclaratively {
    name("getPoints")
    definingClass("Lcom/dss/sdk/internal/media/Insertion;")
    returnType("Ljava/util/List")
}

internal val BytecodePatchContext.insertionGetRangesMethod by gettingFirstMutableMethodDeclaratively {
    name("getRanges")
    definingClass("Lcom/dss/sdk/internal/media/Insertion;")
    returnType("Ljava/util/List")
}

