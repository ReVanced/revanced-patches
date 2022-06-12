package app.revanced.patches.youtube.misc.microg.signatures

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility

@Name("cast-module-signature")
@MatchingMethod(
    "Llqh;", "c"
)
@DirectPatternScanMethod
@MicroGPatchCompatibility
@Version("0.0.1")
object CastDynamiteModuleSignature : MethodSignature(
    null, null, null, null,
    listOf("com.google.android.gms.cast.framework.internal.CastDynamiteModuleImpl")
)