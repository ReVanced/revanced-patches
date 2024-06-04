package app.revanced.patches.tiktok.misc.login.disablerequirement.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val mandatoryLoginServiceFingerprint = methodFingerprint {
    custom { methodDef, classDef ->
        classDef.endsWith("/MandatoryLoginService;") &&
            methodDef.name == "enableForcedLogin"
    }
}
