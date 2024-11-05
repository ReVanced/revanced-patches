package app.revanced.patches.tumblr.annoyances.notifications

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val disableBlogNotificationReminderPatch = bytecodePatch(
    name = "Disable blog notification reminder",
    description = "Disables the reminder to enable notifications for blogs you visit.",
) {
    compatibleWith("com.tumblr")

    execute {
        isBlogNotifyEnabledFingerprint.method.addInstructions(
            0,
            """
                # Return false for BlogNotifyCtaDialog.isEnabled() method.
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
