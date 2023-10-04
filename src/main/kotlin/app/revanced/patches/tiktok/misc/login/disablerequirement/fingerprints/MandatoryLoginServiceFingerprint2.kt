package app.revanced.patches.tiktok.misc.login.disablerequirement.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object MandatoryLoginServiceFingerprint2 : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/MandatoryLoginService;") &&
                methodDef.name == "shouldShowForcedLogin"
    }
)