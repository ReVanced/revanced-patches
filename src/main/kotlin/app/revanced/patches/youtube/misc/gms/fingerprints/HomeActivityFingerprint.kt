package app.revanced.patches.youtube.misc.gms.fingerprints

import app.revanced.patches.shared.integrations.AbstractIntegrationsPatch.IntegrationsFingerprint

object HomeActivityFingerprint : IntegrationsFingerprint(
    customFingerprint = { methodDef, classDef ->
        methodDef.name == "onCreate" && classDef.type.endsWith("Shell_HomeActivity;")
    },
)
