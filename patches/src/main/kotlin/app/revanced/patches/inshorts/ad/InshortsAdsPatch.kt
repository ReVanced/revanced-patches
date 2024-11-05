package app.revanced.patches.inshorts.ad

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide ads",
) {
    compatibleWith("com.nis.app")

    execute {
        inshortsAdsFingerprint.method.addInstruction(
            0,
            """
                return-void
            """,
        )
    }
}
