package app.revanced.patches.viber.ads

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.returnEarly

@Suppress("unused")
val hideAdsPatch = bytecodePatch(
    name = "Hide Ads",
    description = "Hides ad banners between chats.",
) {
    compatibleWith("com.viber.voip")

    execute {
        // Return 1 (true) indicating ads should be disabled.
        adsFreeFingerprint.method.returnEarly(1)
    }
}
