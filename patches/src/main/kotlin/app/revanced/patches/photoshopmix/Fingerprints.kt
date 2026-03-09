package app.revanced.patches.photoshopmix

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.disableLoginMethod by gettingFirstMethodDeclaratively{
    name("isLoggedIn")
    definingClass("CreativeCloudSource;")
    returnType("Z")
}

internal val BytecodePatchContext.libButtonClickedMethod by gettingFirstMethodDeclaratively {
    name("ccLibButtonClickHandler")
    definingClass("PSMixFragment;")
}

internal val BytecodePatchContext.lightroomButtonClickedMethod by gettingFirstMethodDeclaratively {
    name("lightroomButtonClickHandler")
    definingClass("PSMixFragment;")
}

internal val BytecodePatchContext.ccButtonClickedMethod by gettingFirstMethodDeclaratively {
    name("ccButtonClickHandler")
    definingClass("PSMixFragment;")
}
