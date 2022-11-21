package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patches.youtube.misc.integrations.annotations.IntegrationsCompatibility
import app.revanced.shared.patches.AbstractIntegrationsPatch.IntegrationsFingerprint

@Name("service-fingerprint")
@IntegrationsCompatibility
@Version("0.0.1")
object ServiceFingerprint : IntegrationsFingerprint(
    customFingerprint = { methodDef -> methodDef.definingClass.endsWith("ApiPlayerService;") && methodDef.name == "<init>" },
    contextRegisterResolver = { it.implementation!!.registerCount - it.parameters.size }
)