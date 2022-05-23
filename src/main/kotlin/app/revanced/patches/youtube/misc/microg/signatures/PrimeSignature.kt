package app.revanced.patches.youtube.misc.microg.signatures

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.signature.implementation.method.MethodSignature
import app.revanced.patcher.signature.implementation.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.signature.implementation.method.annotation.MatchingMethod
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility

@Name("google-play-prime-signature")
@MatchingMethod(
    "Lpag;", "d"
)
@DirectPatternScanMethod
@MicroGPatchCompatibility
@Version("0.0.1")
object PrimeSignature : MethodSignature(
    null, null, null, null, listOf("com.google.android.GoogleCamera", "com.android.vending")
)