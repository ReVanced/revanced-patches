package app.revanced.patches.moneymanager.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.moneymanager.annotations.UnlockProCompatibility
import app.revanced.patches.moneymanager.fingerprints.UnlockProFingerprint

@Patch
@Name("unlock-pro")
@Description("Unlocks pro features.")
@UnlockProCompatibility
@Version("0.0.1")
class UnlockProPatch : BytecodePatch(
    listOf(UnlockProFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
       UnlockProFingerprint.result!!.mutableMethod.addInstructions(
            0,
            """
                   const/4 v0, 0x1
                   
                   return v0 
                """
        )
        return PatchResultSuccess()
    }
}