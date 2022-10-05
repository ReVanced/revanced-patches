package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.misc.integrations.annotations.IntegrationsCompatibility

@Name("init-fingerprint")
@IntegrationsCompatibility
@Version("0.0.1")
object InitFingerprint : MethodFingerprint(
    strings = listOf("Application creation")
)