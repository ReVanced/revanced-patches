package app.revanced.patches.youtube.layout.sponsorblock.bytecode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.sponsorblock.annotations.SponsorBlockCompatibility

@Name("seek-fingerprint")
@MatchingMethod(
    "Laajv;", "af"
)
@DirectPatternScanMethod
@SponsorBlockCompatibility
@Version("0.0.1")
object SeekFingerprint : MethodFingerprint(
    null,
    null,
    null,
    null,
    listOf("Attempting to seek during an ad")
)