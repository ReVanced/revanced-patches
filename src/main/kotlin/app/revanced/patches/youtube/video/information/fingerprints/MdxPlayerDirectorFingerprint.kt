package app.revanced.patches.youtube.video.information.fingerprints


import app.revanced.patcher.fingerprint.MethodFingerprint

internal object MdxPlayerDirectorFingerprint : MethodFingerprint(
    strings = listOf("MdxDirector setVideoStage ad should be null when videoStage is not an Ad state ")
)
