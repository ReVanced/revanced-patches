package app.revanced.patches.tiktok.misc.login.disablerequirement.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val mandatoryLoginServiceFingerprint2 = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/MandatoryLoginService;") &&
            methodDef.name == "shouldShowForcedLogin"
    }
}
