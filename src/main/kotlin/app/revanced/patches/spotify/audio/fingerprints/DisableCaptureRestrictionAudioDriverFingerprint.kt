package app.revanced.patches.spotify.audio.fingerprints

import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.fingerprint.method.annotation.DirectPatternScanMethod
import app.revanced.patcher.fingerprint.method.annotation.MatchingMethod
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint
import app.revanced.patches.spotify.audio.annotation.DisableCaptureRestrictionCompatibility

@Name("disable-capture-restriction-audio-driver-fingerprint")
@MatchingMethod(
    "Lcom/spotify/playback/playbacknative/AudioDriver;", "constructAudioAttributes"
)
@DirectPatternScanMethod
@DisableCaptureRestrictionCompatibility
@Version("0.0.1")
object DisableCaptureRestrictionAudioDriverFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass == "Lcom/spotify/playback/playbacknative/AudioDriver;" && methodDef.name == "constructAudioAttributes"
    }
)