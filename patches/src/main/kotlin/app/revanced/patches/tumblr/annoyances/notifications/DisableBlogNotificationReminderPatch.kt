package app.revanced.patches.tumblr.annoyances.notifications

import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused")
val `Disable blog notification reminder` by creatingBytecodePatch(
    description = "Disables the reminder to enable notifications for blogs you visit.",
) {
    compatibleWith("com.tumblr")

    apply {
        isBlogNotifyEnabledMethod.addInstructions(
            0,
            """
                # Return false for BlogNotifyCtaDialog.isEnabled() method.
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
