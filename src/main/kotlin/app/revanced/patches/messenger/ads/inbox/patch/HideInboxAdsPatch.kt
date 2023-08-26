package app.revanced.patches.messenger.ads.inbox.patch

import app.revanced.extensions.exception
import app.revanced.patcher.annotation.*
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.messenger.ads.inbox.fingerprints.LoadInboxAdsFingerprint

@Patch
@Name("Hide inbox ads")
@Description("Hides ads in inbox.")
@Compatibility([Package("com.facebook.orca")])
class HideInboxAdsPatch : BytecodePatch(
    listOf(LoadInboxAdsFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        LoadInboxAdsFingerprint.result?.mutableMethod?.apply {
            this.replaceInstruction(0, "return-void")
        } ?: throw LoadInboxAdsFingerprint.exception
    }
}

