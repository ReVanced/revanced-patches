package app.revanced.patches.shared.fingerprints

import app.revanced.patches.shared.integrations.AbstractIntegrationsPatch.IntegrationsFingerprint

object HomeActivityFingerprint : IntegrationsFingerprint(
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "onCreate" && classDef.type.endsWith("Shell_HomeActivity;")
    },
)
