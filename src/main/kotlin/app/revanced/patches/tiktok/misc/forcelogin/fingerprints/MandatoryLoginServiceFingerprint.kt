package app.revanced.patches.tiktok.misc.forcelogin.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.misc.forcelogin.annotations.DisableForceLoginCompatibility

@Name("mandatory-login-service-fingerprint")
@DisableForceLoginCompatibility
@Version("0.0.1")
object MandatoryLoginServiceFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/MandatoryLoginService;") &&
                methodDef.name == "enableForcedLogin"
    }
)