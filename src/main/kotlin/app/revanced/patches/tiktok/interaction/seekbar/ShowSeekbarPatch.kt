package app.revanced.patches.tiktok.interaction.seekbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val showSeekbarPatch = bytecodePatch(
    name = "Show seekbar",
    description = "Shows progress bar for all video.",
) {
    compatibleWith(
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically",
    )

    val shouldShowSeekBarMatch by shouldShowSeekBarFingerprint()
    val setSeekBarShowTypeMatch by setSeekBarShowTypeFingerprint()

    execute {
        shouldShowSeekBarMatch.mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
        setSeekBarShowTypeMatch.mutableMethod.apply {
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
