package app.revanced.patches.facebook.ads.story

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val hideStoryAdsPatch = bytecodePatch(
    name = "Hide story ads",
    description = "Hides the ads in the Facebook app stories.",
) {
    compatibleWith("com.facebook.katana")

    val fetchMoreAdsMatch by fetchMoreAdsFingerprint()
    val adsInsertionMatch by adsInsertionFingerprint()

    execute {
        setOf(fetchMoreAdsMatch, adsInsertionMatch).forEach { match ->
            match.mutableMethod.replaceInstruction(0, "return-void")
        }
    }
}
