package app.revanced.patches.youtube.misc.video.information.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.sponsorblock.annotations.SponsorBlockCompatibility

@Name("create-video-player-seekbar-fingerprint")
@SponsorBlockCompatibility
@Version("0.0.1")
object CreateVideoPlayerSeekbarFingerprint : MethodFingerprint(
    "V",
    strings = listOf("timed_markers_width")
)