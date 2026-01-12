package app.revanced.patches.reddit.customclients.joeyforreddit.ads

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.`Disable piracy detection`

@Suppress("unused", "ObjectPropertyName")
val `Disable ads` by creatingBytecodePatch {
    dependsOn(`Disable piracy detection`)

    compatibleWith("o.o.joey")

    apply {
        isAdFreeUserMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
