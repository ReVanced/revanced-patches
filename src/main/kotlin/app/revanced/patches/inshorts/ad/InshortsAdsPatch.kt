package app.revanced.patches.inshorts.ad

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
) {
    compatibleWith("com.nis.app")

    val inshortsAdsResult by inshortsAdsFingerprint

    execute {
        inshortsAdsResult.mutableMethod.addInstruction(
            0,
            """
                return-void
            """,
        )
    }
}
