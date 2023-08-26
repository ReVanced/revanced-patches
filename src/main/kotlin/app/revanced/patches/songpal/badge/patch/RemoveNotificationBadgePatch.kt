package app.revanced.patches.songpal.badge.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.songpal.badge.annotations.BadgeCompatibility
import app.revanced.patches.songpal.badge.fingerprints.ShowNotificationFingerprint

@Patch
@Name("Remove notification badge")
@Description("Removes the red notification badge from the activity tab.")
@BadgeCompatibility
class RemoveNotificationBadgePatch : BytecodePatch(
    listOf(ShowNotificationFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        ShowNotificationFingerprint.result?.mutableMethod?.apply {
            addInstructions(0, "return-void")
        } ?: throw ShowNotificationFingerprint.exception
    }
}
