package app.revanced.patches.tiktok.misc.forcelogin.fingerprints

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object MandatoryLoginServiceFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/MandatoryLoginService;") &&
                methodDef.name == "enableForcedLogin"
    }
)