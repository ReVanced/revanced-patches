package app.revanced.patches.facebook.ads.story

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.facebook.ads.story.fingerprints.AdsInsertionFingerprint
import app.revanced.patches.facebook.ads.story.fingerprints.FetchMoreAdsFingerprint

@Patch(
    name = "Hide story ads",
    description = "Hides the ads in the Facebook app stories.",
    compatiblePackages = [CompatiblePackage("com.facebook.katana")]
)
@Suppress("unused")
object HideStoryAdsPatch : BytecodePatch(
    setOf(FetchMoreAdsFingerprint, AdsInsertionFingerprint)
) {
    override fun execute(context: BytecodeContext) =
        setOf(FetchMoreAdsFingerprint, AdsInsertionFingerprint).forEach { fingerprint ->
            fingerprint.result?.mutableMethod?.replaceInstruction(0, "return-void")
                ?: throw fingerprint.exception
        }
}
