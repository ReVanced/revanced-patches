package app.revanced.patches.tiktok.misc.login.disablerequirement

internal val BytecodePatchContext.mandatoryLoginServiceMethod by gettingFirstMethodDeclaratively {
    custom { method, classDef ->
        classDef.endsWith("/MandatoryLoginService;") &&
            method.name == "enableForcedLogin"
    }
}

internal val BytecodePatchContext.mandatoryLoginService2Method by gettingFirstMethodDeclaratively {
    custom { method, classDef ->
        classDef.endsWith("/MandatoryLoginService;") &&
            method.name == "shouldShowForcedLogin"
    }
}
