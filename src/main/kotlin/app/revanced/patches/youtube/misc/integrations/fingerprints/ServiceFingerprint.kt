package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patches.shared.integrations.patch.AbstractIntegrationsPatch.IntegrationsFingerprint

object ServiceFingerprint : IntegrationsFingerprint(
    customFingerprint = { methodDef -> methodDef.definingClass.endsWith("ApiPlayerService;") && methodDef.name == "<init>" },
    contextRegisterResolver = { it.implementation!!.registerCount - it.parameters.size }
)