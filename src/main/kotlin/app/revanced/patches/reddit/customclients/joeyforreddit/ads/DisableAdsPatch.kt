package app.revanced.patches.reddit.customclients.joeyforreddit.ads

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.reddit.customclients.joeyforreddit.ads.fingerprints.isAdFreeUserFingerprint
import app.revanced.patches.reddit.customclients.joeyforreddit.detection.piracy.DisablePiracyDetectionPatch

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable ads"
) {
    dependsOn(DisablePiracyDetectionPatch)

    compatibleWith("o.o.joey")

    val isAdFreeUserResult by isAdFreeUserFingerprint

    execute {
        isAdFreeUserResult.mutableMethod.addInstructions(
            0, """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}