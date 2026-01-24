package app.revanced.patches.messenger.inbox

import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Hide inbox subtabs` by creatingBytecodePatch(
    description = "Hides Home and Channels tabs between active now tray and chats.",
) {
    compatibleWith("com.facebook.orca")

    apply {
        createInboxSubTabsMethod.replaceInstruction(2, "const/4 v0, 0x0")
    }
}
