package app.revanced.patches.youtube.layout.miniplayer.fingerprints

import app.revanced.patcher.fingerprint.MethodFingerprint
import app.revanced.patches.youtube.layout.miniplayer.fingerprints.YouTubePlayerOverlaysLayoutFingerprint.YOUTUBE_PLAYER_OVERLAYS_LAYOUT_CLASS_NAME

internal object YouTubePlayerOverlaysLayoutFingerprint : MethodFingerprint(
    customFingerprint = { _, classDef ->
        classDef.type == YOUTUBE_PLAYER_OVERLAYS_LAYOUT_CLASS_NAME
    }
) {
    const val YOUTUBE_PLAYER_OVERLAYS_LAYOUT_CLASS_NAME =
        "Lcom/google/android/apps/youtube/app/common/player/overlay/YouTubePlayerOverlaysLayout;"
}