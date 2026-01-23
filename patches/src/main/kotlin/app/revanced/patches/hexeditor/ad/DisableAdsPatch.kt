package app.revanced.patches.hexeditor.ad

import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Disable ads` by creatingBytecodePatch {
    compatibleWith("com.myprog.hexedit")

    apply {
        primaryAdsMethod.replaceInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
