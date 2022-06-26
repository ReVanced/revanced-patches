package app.revanced.patches.youtube.misc.integrations.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patches.youtube.misc.integrations.annotations.IntegrationsCompatibility

@Name("init-fingerprint")
@MatchingMethod(
    "Lacuu", "onCreate"
)
@IntegrationsCompatibility
@Version("0.0.1")
object InitFingerprint : MethodFingerprint(
    null, null, null, null,
    listOf("Application creation")
)