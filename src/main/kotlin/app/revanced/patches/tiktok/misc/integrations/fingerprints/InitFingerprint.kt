package app.revanced.patches.tiktok.misc.integrations.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patches.tiktok.misc.integrations.annotations.TikTokIntegrationsCompatibility
import app.revanced.shared.patches.AbstractIntegrationsPatch.IntegrationsFingerprint

@Name("init-fingerprint")
@TikTokIntegrationsCompatibility
@Version("0.0.1")
object InitFingerprint : IntegrationsFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/AwemeHostApplication;") &&
                methodDef.name == "onCreate"
    }
)