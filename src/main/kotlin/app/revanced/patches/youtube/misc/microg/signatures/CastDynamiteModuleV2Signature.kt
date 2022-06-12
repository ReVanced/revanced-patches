package app.revanced.patches.youtube.misc.microg.signatures

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility

@Name("cast-context-fetch-signature")
@MatchingMethod(
    "Lmcf;", "c"
)
@DirectPatternScanMethod
@MicroGPatchCompatibility
@Version("0.0.1")
object CastDynamiteModuleV2Signature : MethodSignature(
    null, null, null, null,
    listOf("Failed to load module via V2: ")
)