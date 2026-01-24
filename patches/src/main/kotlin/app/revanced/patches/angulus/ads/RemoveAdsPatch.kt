package app.revanced.patches.angulus.ads

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.shared.misc.pairip.license.`Disable Pairip license check`
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Hide ads` by creatingBytecodePatch {
    compatibleWith("com.drinkplusplus.angulus")

    dependsOn(`Disable Pairip license check`)

    apply {
        // Always return 0 as the daily measurement count.
        getDailyMeasurementCountMethod.returnEarly(0)
    }
}
