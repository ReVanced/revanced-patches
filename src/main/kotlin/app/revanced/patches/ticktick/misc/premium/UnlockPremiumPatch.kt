package app.revanced.patches.ticktick.misc.premium

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.removeInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.ticktick.misc.premium.fingerprints.UserDataIsProFingerprint

@Patch(
    name = "Unlock premium",
    description = "Unlocks premium features.",
    compatiblePackages = [CompatiblePackage("com.ticktick.task")]
)
@Suppress("unused")
object UnlockPremiumPatch : BytecodePatch(setOf(UserDataIsProFingerprint)) {
    override fun execute(context: BytecodeContext) {
        val isUserPremiumMethod = UserDataIsProFingerprint.result!!.mutableMethod
        isUserPremiumMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )
    }
}
