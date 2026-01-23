package app.revanced.patches.tiktok.interaction.seekbar

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Show seekbar` by creatingBytecodePatch(
    description = "Shows progress bar for all video.",
) {
    compatibleWith(
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically",
    )

    apply {
        shouldShowSeekBarMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
        setSeekBarShowTypeMethod.apply {
            val typeRegister = implementation!!.registerCount - 1

            addInstructions(
                0,
                """
                    const/16 v$typeRegister, 0x64
                """,
            )
        }
    }
}
