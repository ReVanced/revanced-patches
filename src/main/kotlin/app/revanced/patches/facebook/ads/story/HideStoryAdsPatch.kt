package app.revanced.patches.facebook.ads.story

import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.facebook.ads.story.fingerprints.adsInsertionFingerprint
import app.revanced.patches.facebook.ads.story.fingerprints.fetchMoreAdsFingerprint

@Suppress("unused")
val hideStoryAdsPatch = bytecodePatch(
    name = "Hide story ads",
    description = "Hides the ads in the Facebook app stories."
) {
    compatibleWith("com.facebook.katana")

    val fetchMoreAdsResult by fetchMoreAdsFingerprint
    val adsInsertionResult by adsInsertionFingerprint

    execute {
        setOf(fetchMoreAdsResult, adsInsertionResult).forEach { result ->
            result.mutableMethod.replaceInstruction(0, "return-void")
        }
    }
}