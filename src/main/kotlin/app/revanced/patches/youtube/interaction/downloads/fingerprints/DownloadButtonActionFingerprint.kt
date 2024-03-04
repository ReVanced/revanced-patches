package app.revanced.patches.youtube.interaction.downloads.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint

internal object DownloadButtonActionFingerprint : MethodFingerprint(
    strings = listOf("offline/get_download_action"),
)
