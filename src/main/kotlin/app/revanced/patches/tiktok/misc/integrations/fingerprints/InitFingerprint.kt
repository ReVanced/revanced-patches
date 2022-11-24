package app.revanced.patches.tiktok.misc.integrations.fingerprints

import app.revanced.patches.shared.integrations.patch.AbstractIntegrationsPatch.IntegrationsFingerprint

object InitFingerprint : IntegrationsFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass.endsWith("/AwemeHostApplication;") &&
                methodDef.name == "onCreate"
    }
)