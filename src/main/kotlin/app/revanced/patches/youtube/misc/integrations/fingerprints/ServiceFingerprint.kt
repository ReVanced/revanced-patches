package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.integrations.annotations.IntegrationsCompatibility

@Name("service-fingerprint")
@IntegrationsCompatibility
@Version("0.0.1")
object ServiceFingerprint : MethodFingerprint(
    customFingerprint = {  methodDef -> methodDef.definingClass.endsWith("ApiPlayerService;") && methodDef.name == "<init>" }
)