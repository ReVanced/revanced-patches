package app.revanced.patches.photomath.misc.unlock.bookpoint

import app.revanced.patcher.extensions.replaceInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Enable bookpoint` by creatingBytecodePatch(
    description = "Enables textbook access",
) {

    apply {
        isBookpointEnabledMethod.replaceInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}
