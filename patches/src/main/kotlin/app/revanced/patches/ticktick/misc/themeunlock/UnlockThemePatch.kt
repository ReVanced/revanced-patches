package app.revanced.patches.ticktick.misc.themeunlock

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.removeInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockThemesPatch = bytecodePatch(
    name = "Unlock themes",
    description = "Unlocks all themes that are inaccessible until a certain level is reached.",
) {
    compatibleWith("com.ticktick.task")

    apply {
        checkLockedThemesMethod.addInstructions(
            0,
            """
            const/4 v0, 0x0
            return v0
            """,
        )

        setThemeMethod.removeInstructions(0, 10)
    }
}
