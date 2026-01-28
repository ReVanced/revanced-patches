package app.revanced.patches.reddit.customclients.joeyforreddit.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.disablePiracyDetectionPatch
import app.revanced.util.returnEarly

@Suppress("unused")
val disableAdsPatch = bytecodePatch("Disable ads") {
    dependsOn(disablePiracyDetectionPatch)

    compatibleWith("o.o.joey")

    apply {
        isAdFreeUserMethod.returnEarly(true)
    }
}
