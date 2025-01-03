package app.revanced.patches.tiktok.misc.login.disablerequirement

import app.revanced.patcher.fingerprint

internal val mandatoryLoginServiceFingerprint by fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/MandatoryLoginService;") &&
            method.name == "enableForcedLogin"
    }
}

internal val mandatoryLoginService2Fingerprint by fingerprint {
    custom { method, classDef ->
        classDef.endsWith("/MandatoryLoginService;") &&
            method.name == "shouldShowForcedLogin"
    }
}
