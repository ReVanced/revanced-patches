package app.revanced.patches.youtube.layout.sponsorblock.bytecode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.sponsorblock.annotations.SponsorBlockCompatibility

@Name("show-player-controls-fingerprint")
@MatchingMethod(
    "LYouTubeControlsOverlay;", "ac"
)
@DirectPatternScanMethod
@SponsorBlockCompatibility
@Version("0.0.1")
object ShowPlayerControlsFingerprint : MethodFingerprint(
    "V", null, listOf("Z","Z"), null,  null
)