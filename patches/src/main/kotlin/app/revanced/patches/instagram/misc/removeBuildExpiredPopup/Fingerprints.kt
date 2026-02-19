
package app.revanced.patches.instagram.misc.removeBuildExpiredPopup

import app.revanced.patcher.gettingFirstMethodDeclaratively
import app.revanced.patcher.instructions
import app.revanced.patcher.invoke
import app.revanced.patcher.patch.BytecodePatchContext

internal const val MILLISECOND_IN_A_DAY_LITERAL = 0x5265c00L

internal val BytecodePatchContext.appUpdateLockoutBuilderMethod by gettingFirstMethodDeclaratively(
    "android.hardware.sensor.hinge_angle",
) {
    instructions(MILLISECOND_IN_A_DAY_LITERAL())
}
