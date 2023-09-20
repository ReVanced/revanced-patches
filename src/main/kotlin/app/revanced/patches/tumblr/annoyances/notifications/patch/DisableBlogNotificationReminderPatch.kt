package app.revanced.patches.tumblr.annoyances.notifications.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tumblr.annoyances.notifications.fingerprints.IsBlogNotifyEnabledFingerprint

@Patch(
    name = "Disable blog notification reminder",
    description = "Disables the reminder to enable notifications for blogs you visit.",
    compatiblePackages = [CompatiblePackage("com.tumblr")]
)
@Suppress("unused")
object DisableBlogNotificationReminderPatch : BytecodePatch(
    setOf(IsBlogNotifyEnabledFingerprint)
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