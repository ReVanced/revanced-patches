package app.revanced.patches.tiktok.misc.login.disablerequirement

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.mandatoryLoginServiceMethod by gettingFirstMethodDeclaratively {
    name("enableForcedLogin")
    definingClass { endsWith("/MandatoryLoginService;") }
}

internal val BytecodePatchContext.mandatoryLoginService2Method by gettingFirstMethodDeclaratively {
    name("shouldShowForcedLogin")
    definingClass { endsWith("/MandatoryLoginService;") }
}
