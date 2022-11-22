package app.revanced.patches.spotify.audio.fingerprints


import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint

object DisableCaptureRestrictionAudioDriverFingerprint : MethodFingerprint(
    customFingerprint = { methodDef ->
        methodDef.definingClass == "Lcom/spotify/playback/playbacknative/AudioDriver;" && methodDef.name == "constructAudioAttributes"
    }
)