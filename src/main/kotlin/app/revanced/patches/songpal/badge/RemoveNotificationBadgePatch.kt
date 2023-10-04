package app.revanced.patches.songpal.badge

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.songpal.badge.fingerprints.ShowNotificationFingerprint

@Patch(
    name = "Remove notification badge",
    description = "Removes the red notification badge from the activity tab.",
    compatiblePackages = [CompatiblePackage("com.sony.songpal.mdr")]
)
@Suppress("unused")
object RemoveNotificationBadgePatch : BytecodePatch(setOf(ShowNotificationFingerprint)) {
    override fun execute(context: BytecodeContext) {
        ShowNotificationFingerprint.result?.mutableMethod?.addInstructions(0, "return-void")
            ?: throw ShowNotificationFingerprint.exception
    }
}
