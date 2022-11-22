package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.shared.patches.AbstractIntegrationsPatch.IntegrationsFingerprint

object ServiceFingerprint : IntegrationsFingerprint(
    customFingerprint = { methodDef -> methodDef.definingClass.endsWith("ApiPlayerService;") && methodDef.name == "<init>" },
    contextRegisterResolver = { it.implementation!!.registerCount - it.parameters.size }
)