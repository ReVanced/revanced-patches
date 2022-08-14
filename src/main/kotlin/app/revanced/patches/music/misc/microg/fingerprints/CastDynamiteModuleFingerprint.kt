package app.revanced.patches.music.misc.microg.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility

@Name("cast-module-fingerprint")
@MatchingMethod(
    "Llqh;", "c"
)
@DirectPatternScanMethod
@MicroGPatchCompatibility
@Version("0.0.1")
object CastDynamiteModuleFingerprint : MethodFingerprint(
    null, null, null, null,
    listOf("com.google.android.gms.cast.framework.internal.CastDynamiteModuleImpl")
)