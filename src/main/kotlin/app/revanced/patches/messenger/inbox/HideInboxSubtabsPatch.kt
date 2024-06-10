package app.revanced.patches.messenger.inbox

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.messenger.inbox.fingerprints.createInboxSubTabsFingerprint

@Suppress("unused")
val hideInboxSubtabsPatch = bytecodePatch(
    name = "Hide inbox subtabs",
    description = "Hides Home and Channels tabs between active now tray and chats.",
) {
    compatibleWith("com.facebook.orca")

    val createInboxSubTabsResult by createInboxSubTabsFingerprint

    execute {
        createInboxSubTabsResult.mutableMethod.replaceInstruction(2, "const/4 v0, 0x0")
    }
}
