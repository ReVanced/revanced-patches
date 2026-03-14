package app.revanced.patches.instagram.reels.autoscroll

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.parameterTypes
import app.revanced.patcher.patch.BytecodePatchContext
import app.revanced.patcher.returnType

/**
 * Matches the feature availability gate that determines
 * whether auto-scroll should be available for Reels.
 */
internal val BytecodePatchContext.clipsAutoScrollFeatureCheckMethod by gettingFirstMethodDeclaratively("auto_scroll") {
    returnType("Z")
    parameterTypes("Lcom/instagram/common/session/UserSession;", "Z")
}

/**
 * Matches the toggle handler called when the user taps
 * the auto-scroll button. Contains analytics logging strings.
 */
internal val BytecodePatchContext.clipsAutoScrollToggleMethod by gettingFirstMethodDeclaratively(
    "auto_scroll",
    "instagram_clips_viewer_autoplay_tap",
)
