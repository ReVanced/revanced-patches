package app.revanced.patches.youtube.misc.integrations.signatures

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patches.youtube.misc.integrations.annotations.IntegrationsCompatibility

@Name("init-signature")
@MatchingMethod(
    "Lacuu", "onCreate"
)
@IntegrationsCompatibility
@Version("0.0.1")
object InitSignature : MethodSignature(
    null, null, null, null,
    listOf("Application creation")
)