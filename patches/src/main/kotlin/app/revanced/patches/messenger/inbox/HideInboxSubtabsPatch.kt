package app.revanced.patches.messenger.inbox

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideInboxSubtabsPatch = bytecodePatch(
    name = "Hide inbox subtabs",
    description = "Hides Home and Channels tabs between active now tray and chats.",
) {
    compatibleWith("com.facebook.orca")

    execute {
        createInboxSubTabsFingerprint.method.replaceInstruction(2, "const/4 v0, 0x0")
    }
}
