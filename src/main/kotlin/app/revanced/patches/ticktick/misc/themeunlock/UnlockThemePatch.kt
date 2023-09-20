package app.revanced.patches.ticktick.misc.themeunlock

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.ticktick.misc.themeunlock.fingerprints.CheckLockedThemesFingerprint
import app.revanced.patches.ticktick.misc.themeunlock.fingerprints.SetThemeFingerprint

@Patch(
    name = "Unlock themes",
    description = "Unlocks all themes that are inaccessible until a certain level is reached.",
    compatiblePackages = [CompatiblePackage("com.ticktick.task")]
)
@Suppress("unused")
object UnlockProPatch : BytecodePatch(setOf(CheckLockedThemesFingerprint, SetThemeFingerprint)) {
    override fun execute(context: BytecodeContext) {
        val lockedThemesMethod = CheckLockedThemesFingerprint.result!!.mutableMethod
        lockedThemesMethod.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """
        )
        
        val setThemeMethod = SetThemeFingerprint.result!!.mutableMethod
        setThemeMethod.removeInstructions(0, 10)
    }
}
