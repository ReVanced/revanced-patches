package app.revanced.patches.twitch.debug

import app.revanced.patcher.definingClass
import app.revanced.patcher.gettingFirstMutableMethodDeclaratively
import app.revanced.patcher.name
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.isDebugConfigEnabledMethod by gettingFirstMutableMethodDeclaratively {
    name("isDebugConfigEnabled")
    definingClass { endsWith("/BuildConfigUtil;") }
}

internal val BytecodePatchContext.isOmVerificationEnabledMethod by gettingFirstMutableMethodDeclaratively {
    name("isOmVerificationEnabled")
    definingClass { endsWith("/BuildConfigUtil;") }
}

internal val BytecodePatchContext.shouldShowDebugOptionsMethod by gettingFirstMutableMethodDeclaratively {
    name("shouldShowDebugOptions")
    definingClass { endsWith("/BuildConfigUtil;") }
}
