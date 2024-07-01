package app.revanced.patches.windyapp.misc.unlockpro

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.windyapp.misc.unlockpro.fingerprints.CheckProFingerprint

@Patch(
    name = "Unlock pro",
    description = "Unlocks all pro features.",
    compatiblePackages = [CompatiblePackage("co.windyapp.android")]
)
@Suppress("unused")
object UnlockProPatch : BytecodePatch(
    setOf(CheckProFingerprint)
) {
    override fun execute(context: BytecodeContext) {
         val method = CheckProFingerprint.result!!.mutableMethod
        method.addInstructions(
            0,
            """
                const/16 v0, 0x1
                return v0
            """
        )
    }
}
