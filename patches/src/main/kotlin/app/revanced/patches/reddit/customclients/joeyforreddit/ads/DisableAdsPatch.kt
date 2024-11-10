package app.revanced.patches.reddit.customclients.joeyforreddit.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.disablePiracyDetectionPatch

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable ads",
) {
    dependsOn(disablePiracyDetectionPatch)

    compatibleWith("o.o.joey")

    execute {
        isAdFreeUserFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
