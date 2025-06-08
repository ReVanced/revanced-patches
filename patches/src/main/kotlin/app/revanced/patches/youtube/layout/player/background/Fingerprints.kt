package app.revanced.patches.youtube.layout.player.background

import app.revanced.patcher.fingerprint
import app.revanced.util.literal

internal val inflateControlsGroupLayoutStubFingerprint = fingerprint {
    literal { youtubeControlsButtonGroupLayoutStubResId }
}
