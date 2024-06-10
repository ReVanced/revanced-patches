package app.revanced.patches.youtube.layout.miniplayer.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal const val YOUTUBE_PLAYER_OVERLAYS_LAYOUT_CLASS_NAME =
    "Lcom/google/android/apps/youtube/app/common/player/overlay/YouTubePlayerOverlaysLayout;"

internal val youTubePlayerOverlaysLayoutFingerprint = methodFingerprint {
    custom { _, classDef ->
        classDef.type == YOUTUBE_PLAYER_OVERLAYS_LAYOUT_CLASS_NAME
    }
}
