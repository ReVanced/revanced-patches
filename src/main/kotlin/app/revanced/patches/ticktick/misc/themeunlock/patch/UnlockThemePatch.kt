package app.revanced.patches.ticktick.misc.themeunlock.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.extensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.ticktick.misc.themeunlock.annotations.UnlockThemesCompatibility
import app.revanced.patches.ticktick.misc.themeunlock.fingerprints.CheckLockedThemesFingerprint
import app.revanced.patches.ticktick.misc.themeunlock.fingerprints.SetThemeFingerprint

@Patch
@Name("unlock-themes")
@Description("Unlocks all themes that are inaccessible until a certain level is reached.")
@UnlockThemesCompatibility
@Version("0.0.1")
class UnlockProPatch : BytecodePatch(
    listOf(
        CheckLockedThemesFingerprint,
        SetThemeFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val lockedThemesMethod = CheckLockedThemesFingerprint.result!!.mutableMethod
        lockedThemesMethod.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        )
        
        val setThemeMethod = SetThemeFingerprint.result!!.mutableMethod
        setThemeMethod.removeInstructions(0, 9)
        
        return PatchResultSuccess()
    }
}
