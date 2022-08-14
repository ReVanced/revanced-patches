package app.revanced.patches.music.misc.microg.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patches.youtube.misc.microg.annotations.MicroGPatchCompatibility

@Name("cast-context-fetch-fingerprint")
@MatchingMethod(
    "Lvvz;", "a"
)
@DirectPatternScanMethod
@MicroGPatchCompatibility
@Version("0.0.1")
object CastContextFetchFingerprint : MethodFingerprint(
    null, null, null, null,
    listOf("Error fetching CastContext.")
)