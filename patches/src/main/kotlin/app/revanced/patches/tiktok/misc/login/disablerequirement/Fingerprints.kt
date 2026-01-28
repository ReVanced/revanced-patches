package app.revanced.patches.tiktok.misc.login.disablerequirement

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.mandatoryLoginServiceMethod by gettingFirstMutableMethodDeclaratively {
    name("enableForcedLogin")
    definingClass { endsWith("/MandatoryLoginService;") }
}

internal val BytecodePatchContext.mandatoryLoginService2Method by gettingFirstMutableMethodDeclaratively {
    name("shouldShowForcedLogin")
    definingClass { endsWith("/MandatoryLoginService;") }
}
