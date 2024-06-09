package app.revanced.patches.rar.misc.hidepurchasereminder

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.rar.misc.hidepurchasereminder.fingerprints.HidePurchaseReminderFingerprint

@Patch(
    name = "Hide purchase reminder",
    description = "Hides the support purchase popup.",
    compatiblePackages = [CompatiblePackage("com.rarlab.rar")]
)
@Suppress("unused")
object HidePurchaseReminderPatch : BytecodePatch(
    setOf(HidePurchaseReminderFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        HidePurchaseReminderFingerprint.result?.let { result ->
            result.mutableMethod.addInstruction(0, "return-void")
        }
    }
}