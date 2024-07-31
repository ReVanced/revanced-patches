package app.revanced.patches.messenger.inbox

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideInboxAdsPatch = bytecodePatch(
    name = "Hide inbox ads",
    description = "Hides ads in inbox.",
) {
    compatibleWith("com.facebook.orca")

    val loadInboxAdsMatch by loadInboxAdsFingerprint()

    execute {
        loadInboxAdsMatch.mutableMethod.replaceInstruction(0, "return-void")
    }
}
