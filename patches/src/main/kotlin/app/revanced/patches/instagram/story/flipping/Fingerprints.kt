package app.revanced.patches.instagram.story.flipping

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.onStoryTimeoutActionMethod by gettingFirstMethodDeclaratively("userSession") {
    parameterTypes("Ljava/lang/Object;")
    returnType("V")
    definingClass("Linstagram/features/stories/fragment/ReelViewerFragment;")
}
