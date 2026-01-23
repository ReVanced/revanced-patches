package app.revanced.patches.iconpackstudio.misc.pro

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Unlock pro` by creatingBytecodePatch {
    compatibleWith("ginlemon.iconpackstudio"("2.2 build 016"))

    apply {
        checkProMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
