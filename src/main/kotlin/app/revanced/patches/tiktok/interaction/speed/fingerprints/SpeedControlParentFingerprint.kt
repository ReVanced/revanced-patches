package app.revanced.patches.tiktok.interaction.speed.fingerprints

import app.revanced.patcher.extensions.or
import app.revanced.patcher.fingerprint.MethodFingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object SpeedControlParentFingerprint : MethodFingerprint(
    strings = listOf(
        "onStopTrackingTouch, hasTouchMove=",
        ", isCurVideoPaused: ",
        "already_shown_edge_speed_guide"
    )
)