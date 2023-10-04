package app.revanced.patches.moneymanager

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.moneymanager.fingerprints.UnlockProFingerprint

@Patch(
    name = "Unlock pro",
    compatiblePackages = [CompatiblePackage("com.ithebk.expensemanager")]
)
@Suppress("unused")
object UnlockProPatch : BytecodePatch(
    setOf(UnlockProFingerprint)
){
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