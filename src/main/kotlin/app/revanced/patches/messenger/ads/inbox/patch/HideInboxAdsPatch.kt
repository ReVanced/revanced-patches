package app.revanced.patches.messenger.ads.inbox.patch

import app.revanced.extensions.toErrorResult
import app.revanced.patcher.annotation.*
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.PatchResultSuccess
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.messenger.ads.inbox.fingerprints.LoadInboxAdsFingerprint

@Patch
@Name("hide-inbox-ads")
@Description("Hides ads in inbox.")
@Compatibility([Package("com.facebook.orca")])
@Version("0.0.1")
class HideInboxAdsPatch : BytecodePatch(
    listOf(LoadInboxAdsFingerprint)
) {
    override fun execute(context: BytecodeContext): PatchResult {
        LoadInboxAdsFingerprint.result?.mutableMethod?.apply {
            this.replaceInstruction(0, "return-void")
        } ?: return LoadInboxAdsFingerprint.toErrorResult()

        return PatchResultSuccess()
    }
}

