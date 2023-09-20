package app.revanced.patches.messenger.ads.inbox.patch

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.messenger.ads.inbox.fingerprints.LoadInboxAdsFingerprint

@Patch(
    name = "Hide inbox ads",
    description = "Hides ads in inbox.",
    compatiblePackages = [CompatiblePackage("com.facebook.orca")]
)
@Suppress("unused")
object HideInboxAdsPatch : BytecodePatch(
    setOf(LoadInboxAdsFingerprint)
) {
    override fun execute(context: BytecodeContext) {
        LoadInboxAdsFingerprint.result?.mutableMethod?.apply {
            this.replaceInstruction(0, "return-void")
        } ?: throw LoadInboxAdsFingerprint.exception
    }
}

