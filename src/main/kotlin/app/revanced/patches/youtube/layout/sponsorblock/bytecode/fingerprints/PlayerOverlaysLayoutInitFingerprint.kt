package app.revanced.patches.youtube.layout.sponsorblock.bytecode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.sponsorblock.annotations.SponsorBlockCompatibility

@Name("player-overlays-layout-init-fingerprint")

@SponsorBlockCompatibility
@Version("0.0.1")
object PlayerOverlaysLayoutInitFingerprint : MethodFingerprint(

    customFingerprint = { methodDef -> methodDef.returnType.endsWith("YouTubePlayerOverlaysLayout;") }
)