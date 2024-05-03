package app.revanced.patches.tiktok.interaction.seekbar

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tiktok.interaction.seekbar.fingerprints.setSeekBarShowTypeFingerprint
import app.revanced.patches.tiktok.interaction.seekbar.fingerprints.shouldShowSeekBarFingerprint

@Suppress("unused")
val showSeekbarPatch = bytecodePatch(
    name = "Show seekbar",
    description = "Shows progress bar for all video.",
) {
    compatibleWith(
        "com.ss.android.ugc.trill"(), 
        "com.zhiliaoapp.musically"()
    )

    val shouldShowSeekBarResult by shouldShowSeekBarFingerprint
    val setSeekBarShowTypeResult by setSeekBarShowTypeFingerprint

    execute {
        shouldShowSeekBarResult.mutableMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
        setSeekBarShowTypeResult.mutableMethod.apply {
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
