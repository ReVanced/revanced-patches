package app.revanced.patches.messenger.inbox

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.messenger.inbox.fingerprints.CreateInboxSubTabsFingerprint
import app.revanced.util.exception

@Patch(
    name = "Hide inbox subtabs",
    description = "Hides Home and Channels tabs between active now tray and chats.",
    compatiblePackages = [CompatiblePackage("com.facebook.orca")],
)
@Suppress("unused")
object HideInboxSubtabsPatch : BytecodePatch(
    setOf(CreateInboxSubTabsFingerprint),
) {
    // Set InboxSubtabsItemSupplierImplementation boolean attribute to false.
    override fun execute(context: BytecodeContext) = CreateInboxSubTabsFingerprint.result?.mutableMethod
        ?.replaceInstruction(2, "const/4 v0, 0x0")
        ?: throw CreateInboxSubTabsFingerprint.exception
}
