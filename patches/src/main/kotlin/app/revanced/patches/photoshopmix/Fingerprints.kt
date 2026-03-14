package app.revanced.patches.photoshopmix

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

internal val BytecodePatchContext.isLoggedInMethod by gettingFirstMethodDeclaratively {
    name("isLoggedIn")
    definingClass("CreativeCloudSource;")
    returnType("Z")
}

internal val BytecodePatchContext.ccLibButtonClickHandlerMethod by gettingFirstMethodDeclaratively {
    name("ccLibButtonClickHandler")
    definingClass("PSMixFragment;")
}

internal val BytecodePatchContext.lightroomButtonClickHandlerMethod by gettingFirstMethodDeclaratively {
    name("lightroomButtonClickHandler")
    definingClass("PSMixFragment;")
}

internal val BytecodePatchContext.ccButtonClickHandlerMethod by gettingFirstMethodDeclaratively {
    name("ccButtonClickHandler")
    definingClass("PSMixFragment;")
}
