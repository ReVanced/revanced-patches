package app.revanced.patches.twitch.debug

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.isDebugConfigEnabledMethod by gettingFirstMethodDeclaratively {
    name("isDebugConfigEnabled")
    definingClass { endsWith("/BuildConfigUtil;") }
}

internal val BytecodePatchContext.isOmVerificationEnabledMethod by gettingFirstMethodDeclaratively {
    name("isOmVerificationEnabled")
    definingClass { endsWith("/BuildConfigUtil;") }
}

internal val BytecodePatchContext.shouldShowDebugOptionsMethod by gettingFirstMethodDeclaratively {
    name("shouldShowDebugOptions")
    definingClass { endsWith("/BuildConfigUtil;") }
}
