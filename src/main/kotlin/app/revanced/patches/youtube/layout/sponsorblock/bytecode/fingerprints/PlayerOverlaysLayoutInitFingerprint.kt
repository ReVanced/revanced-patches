package app.revanced.patches.youtube.layout.sponsorblock.bytecode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.sponsorblock.annotations.SponsorBlockCompatibility

@Name("player-overlays-layout-init-fingerprint")
@MatchingMethod(
    "Lihh;", "u"
)
@DirectPatternScanMethod
@SponsorBlockCompatibility
@Version("0.0.1")
object PlayerOverlaysLayoutInitFingerprint : MethodFingerprint(
    null, null, null,
    null,
    null,
    { methodDef -> methodDef.returnType.endsWith("YouTubePlayerOverlaysLayout;") }
)