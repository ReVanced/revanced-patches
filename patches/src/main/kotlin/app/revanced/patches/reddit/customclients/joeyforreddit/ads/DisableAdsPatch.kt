package app.revanced.patches.reddit.customclients.joeyforreddit.ads

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.disablePiracyDetectionPatch
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Disable ads` by creatingBytecodePatch {
    dependsOn(disablePiracyDetectionPatch)

    compatibleWith("o.o.joey")

    apply {
        isAdFreeUserMethod.returnEarly(true)
    }
}
