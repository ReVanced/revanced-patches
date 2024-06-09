package app.revanced.patches.messenger.ads.inbox.patch

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.messenger.ads.inbox.fingerprints.loadInboxAdsFingerprint

@Suppress("unused")
val hideInboxAdsPatch = bytecodePatch(
    name = "Hide inbox ads",
    description = "Hides ads in inbox."
) {
    compatibleWith("com.facebook.orca")

    val loadInboxAdsResult by loadInboxAdsFingerprint

    execute {
        loadInboxAdsResult.mutableMethod.replaceInstruction(0, "return-void")
    }
}