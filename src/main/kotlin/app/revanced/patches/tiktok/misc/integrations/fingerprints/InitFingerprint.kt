package app.revanced.patches.tiktok.misc.integrations.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.tiktok.misc.integrations.annotations.TikTokIntegrationsCompatibility

@Name("init-fingerprint")
@TikTokIntegrationsCompatibility
@Version("0.0.1")
object InitFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/AwemeHostApplication;") &&
                methodDef.name == "onCreate"
    }
)