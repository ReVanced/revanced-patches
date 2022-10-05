package app.revanced.patches.youtube.layout.sponsorblock.bytecode.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version

import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.youtube.layout.sponsorblock.annotations.SponsorBlockCompatibility

@Name("video-time-fingerprint")
@SponsorBlockCompatibility
@Version("0.0.1")
object VideoTimeFingerprint : MethodFingerprint(
    strings = listOf("MedialibPlayerTimeInfo{currentPositionMillis=")
)