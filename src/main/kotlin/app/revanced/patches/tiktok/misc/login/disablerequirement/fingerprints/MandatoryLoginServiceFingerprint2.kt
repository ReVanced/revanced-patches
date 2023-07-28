package app.revanced.patches.tiktok.misc.login.disablerequirement.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.misc.login.disablerequirement.annotations.DisableLoginRequirementCompatibility

@Name("Mandatory login service fingerprint2")
@DisableLoginRequirementCompatibility
object MandatoryLoginServiceFingerprint2 : MethodFingerprint(
    customFingerprint = { methodDef, _ ->
        methodDef.definingClass.endsWith("/MandatoryLoginService;") &&
                methodDef.name == "shouldShowForcedLogin"
    }
)