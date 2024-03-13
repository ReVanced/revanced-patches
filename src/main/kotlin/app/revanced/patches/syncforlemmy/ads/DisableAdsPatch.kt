package app.revanced.patches.syncforlemmy.ads

import app.revanced.util.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.syncforlemmy.ads.fingerprints.IsAdsEnabledFingerprint

@Patch(
    name = "Disable ads",
    description = "This Patch disables all ads in the app",
    compatiblePackages = [CompatiblePackage("io.syncapps.lemmy_sync")]
)
@Suppress("unused")
object DisableAdsPatch : BytecodePatch(setOf(IsAdsEnabledFingerprint)) {
    override fun execute(context: BytecodeContext) {
        IsAdsEnabledFingerprint.result?.mutableMethod?.apply {
            addInstructions(
                0,
                """
                const/4 v0, 0x0
                return v0
            """
            )
        } ?: throw IsAdsEnabledFingerprint.exception
    }
}