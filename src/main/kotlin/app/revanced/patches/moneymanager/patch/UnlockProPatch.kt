package app.revanced.patches.moneymanager.patch

import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.moneymanager.annotations.UnlockProCompatibility
import app.revanced.patches.moneymanager.fingerprints.UnlockProFingerprint

@Patch
@Name("Unlock pro")
@Description("Unlocks pro features.")
@UnlockProCompatibility
class UnlockProPatch : BytecodePatch(
    listOf(UnlockProFingerprint)
) {
    override fun execute(context: BytecodeContext) {
       UnlockProFingerprint.result!!.mutableMethod.addInstructions(
            0,
            """
               const/4 v0, 0x1
               return v0 
            """
        )
    }
}