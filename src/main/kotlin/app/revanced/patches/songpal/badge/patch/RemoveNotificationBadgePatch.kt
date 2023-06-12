package app.revanced.patches.songpal.badge.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.songpal.badge.annotations.BadgeCompatibility
import app.revanced.patches.songpal.badge.fingerprints.ShowNotificationFingerprint

@Patch
@Name("remove-notification-badge")
@Description("Removes the red notification badge from the activity tab.")
@BadgeCompatibility
@Version("0.0.1")
class RemoveNotificationBadgePatch : BytecodePatch(
    listOf(ShowNotificationFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        ShowNotificationFingerprint.result?.mutableMethod?.apply {
            addInstructions(0, "return-void")
        } ?: return ShowNotificationFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}
