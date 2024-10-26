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

    val checkLockedThemesMatch by checkLockedThemesFingerprint()
    val setThemeMatch by setThemeFingerprint()

    execute {
        checkLockedThemesMatch.mutableMethod.addInstructions(
            0,
            """
            const/4 v0, 0x0
            return v0
            """,
        )

        setThemeMatch.mutableMethod.removeInstructions(0, 10)
    }
}
