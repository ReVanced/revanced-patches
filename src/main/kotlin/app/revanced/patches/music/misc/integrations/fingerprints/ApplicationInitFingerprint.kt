package app.revanced.patches.music.misc.integrations.fingerprints

import app.revanced.patches.shared.misc.integrations.BaseIntegrationsPatch.IntegrationsFingerprint

internal object ApplicationInitFingerprint : IntegrationsFingerprint(
    returnType = "V",
    parameters = emptyList(),
    strings = listOf("activity"),
    customFingerprint = { methodDef, _ -> methodDef.name == "onCreate" },
)
