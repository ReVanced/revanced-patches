package app.revanced.patches.facebook.ads.story

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideStoryAdsPatch = bytecodePatch(
    name = "Hide story ads",
    description = "Hides the ads in the Facebook app stories.",
) {
    compatibleWith("com.facebook.katana")

    execute {
        setOf(
            fetchMoreAdsFingerprint,
            adsInsertionFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.replaceInstruction(0, "return-void")
        }
    }
}
