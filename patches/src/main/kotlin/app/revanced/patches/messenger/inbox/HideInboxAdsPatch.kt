package app.revanced.patches.messenger.inbox

import app.revanced.patcher.extensions.replaceInstruction
import app.revanced.patcher.patch.creatingBytecodePatch

@Suppress("unused", "ObjectPropertyName")
val `Hide inbox ads` by creatingBytecodePatch(
    description = "Hides ads in inbox.",
) {
    compatibleWith("com.facebook.orca")

    apply {
        loadInboxAdsMethod.replaceInstruction(0, "return-void")
    }
}
