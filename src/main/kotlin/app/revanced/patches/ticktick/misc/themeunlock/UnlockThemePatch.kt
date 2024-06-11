package app.revanced.patches.ticktick.misc.themeunlock

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock themes",
    description = "Unlocks all themes that are inaccessible until a certain level is reached.",
) {
    compatibleWith("com.ticktick.task")

    val checkLockedThemesResult by checkLockedThemesFingerprint
    val setThemeResult by setThemeFingerprint

    execute {
        checkLockedThemesResult.mutableMethod.addInstructions(
            0,
            """
            const/4 v0, 0x0
            return v0
            """,
        )

        setThemeResult.mutableMethod.removeInstructions(0, 10)
    }
}