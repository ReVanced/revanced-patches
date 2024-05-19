package app.revanced.patches.messenger.inbox.subtabs.patch

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.messenger.inbox.subtabs.fingerprints.CreateInboxSubtabsFingerprint

@Patch(
    name = "Hide inbox subtabs",
    description = "Hides Home and Channels tabs between active now tray and chats.",
    compatiblePackages = [CompatiblePackage("com.facebook.orca")]
)
@Suppress("unused")
object HideChannelSubTab : BytecodePatch(
    setOf(CreateInboxSubtabsFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        CreateInboxSubtabsFingerprint.result?.mutableMethod?.apply {
            this.replaceInstruction(2,
                """
                    # Set InboxSubtabsItemSupplierImplementation boolean attribute to false.
                    const/4 v0, 0x0
                """.trimIndent()
                )
        } ?: throw CreateInboxSubtabsFingerprint.exception
    }
}