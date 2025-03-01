package app.revanced.patches.youtube.misc.zoomhaptics

import app.revanced.patcher.fingerprint
import app.revanced.patcher.string

internal val zoomHapticsFingerprint by fingerprint {
    instructions(
        string("Failed to haptics vibrate for video zoom"),
    )
}
