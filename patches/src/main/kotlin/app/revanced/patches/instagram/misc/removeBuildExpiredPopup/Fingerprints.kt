
package app.revanced.patches.instagram.misc.removeBuildExpiredPopup

import app.revanced.patcher.fingerprint
import app.revanced.util.literal


internal val invokeMethodFingerprint = fingerprint {
    strings("android.hardware.sensor.hinge_angle")
    literal { 0x5265c00L }
}
