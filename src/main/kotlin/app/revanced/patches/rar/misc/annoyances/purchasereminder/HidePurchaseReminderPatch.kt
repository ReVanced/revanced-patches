package app.revanced.patches.rar.misc.annoyances.purchasereminder

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.rar.misc.annoyances.purchasereminder.fingerprints.ShowReminderFingerprint
import app.revanced.util.exception

@Patch(
    name = "Hide purchase reminder",
    description = "Hides the popup that reminds you to purchase the app.",
    compatiblePackages = [CompatiblePackage("com.rarlab.rar")],
)
@Suppress("unused")
object HidePurchaseReminderPatch : BytecodePatch(
    setOf(ShowReminderFingerprint),
) {
    override fun execute(context: BytecodeContext) {
        ShowReminderFingerprint.result?.mutableMethod?.addInstruction(0, "return-void")
            ?: throw ShowReminderFingerprint.exception
    }
}
