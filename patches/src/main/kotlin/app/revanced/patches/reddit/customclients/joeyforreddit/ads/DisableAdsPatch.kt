package app.revanced.patches.reddit.customclients.joeyforreddit.ads

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.`Disable piracy detection`
import app.revanced.util.returnEarly

@Suppress("unused", "ObjectPropertyName")
val `Disable ads` by creatingBytecodePatch {
    dependsOn(`Disable piracy detection`)

    compatibleWith("o.o.joey")

    apply {
        isAdFreeUserMethod.returnEarly(true)
    }
}
