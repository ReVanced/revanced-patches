package app.revanced.patches.tumblr.annoyances.notifications.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.Compatibility
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Package
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.tumblr.annoyances.notifications.fingerprints.IsBlogNotifyEnabledFingerprint

@Patch
@Name("Disable blog notification reminder")
@Description("Disables the reminder to enable notifications for blogs you visit.")
@Compatibility([Package("com.tumblr")])
class DisableBlogNotificationReminderPatch : BytecodePatch(
    listOf(IsBlogNotifyEnabledFingerprint)
) {
    override fun execute(context: BytecodeContext) =
        IsBlogNotifyEnabledFingerprint.result?.mutableMethod?.addInstructions(
            0,
            """
                # Return false for BlogNotifyCtaDialog.isEnabled() method.
                const/4 v0, 0x0
                return v0
            """
        ) ?: throw IsBlogNotifyEnabledFingerprint.exception
}