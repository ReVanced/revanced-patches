package app.revanced.patches.photoshopmix

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.disableLoginMethod by gettingFirstMethodDeclaratively("isLoggedIn") {
    returnType("Z")
    definingClass("/CreativeCloudSource;")
}

internal val BytecodePatchContext.libButtonClickedMethod by gettingFirstMethodDeclaratively("ccLibButtonClickHandler") {
    definingClass("/PSMixFragment;")
}

internal val BytecodePatchContext.lightroomButtonClickedMethod by gettingFirstMethodDeclaratively("lightroomButtonClickHandler") {
    definingClass("/PSMixFragment;")
}

internal val BytecodePatchContext.ccButtonClickedMethod by gettingFirstMethodDeclaratively("ccButtonClickHandler") {
    definingClass("/PSMixFragment;")
}
