
package app.revanced.patches.instagram.misc.removeBuildExpiredPopup

import app.revanced.patcher.fingerprint
import app.revanced.util.literal

internal const val MILLISECOND_IN_A_DAY_LITERAL = 0x5265c00L

internal val appUpdateLockoutBuilderFingerprint = fingerprint {
    strings("android.hardware.sensor.hinge_angle")
    literal { MILLISECOND_IN_A_DAY_LITERAL }
}
