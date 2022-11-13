package app.revanced.patches.twitch.debug.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.twitch.debug.annotations.DebugModeCompatibility

@Name("is-om-verification-enabled-fingerprint")
@DebugModeCompatibility
@Version("0.0.1")
object IsOmVerificationEnabledFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("BuildConfigUtil;") && methodDef.name == "isOmVerificationEnabled"
    }
)