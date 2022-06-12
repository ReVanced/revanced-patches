package app.revanced.patches.youtube.misc.microg.signatures

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility

@Name("cast-context-fetch-signature")
@MatchingMethod(
    "Lvvz;", "a"
)
@DirectPatternScanMethod
@MicroGPatchCompatibility
@Version("0.0.1")
object CastContextFetchSignature : MethodSignature(
    null, null, null, null,
    listOf("Error fetching CastContext.")
)