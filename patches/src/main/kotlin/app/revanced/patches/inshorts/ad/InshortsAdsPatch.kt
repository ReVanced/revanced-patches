package app.revanced.patches.inshorts.ad

import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Hide ads` by creatingBytecodePatch {
    compatibleWith("com.nis.app")

    apply {
        inshortsAdsMethod.addInstruction(
            0,
            """
                return-void
            """,
        )
    }
}
