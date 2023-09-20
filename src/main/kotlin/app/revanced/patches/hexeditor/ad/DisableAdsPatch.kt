package app.revanced.patches.hexeditor.ad

import app.revanced.extensions.exception
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstructions
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.hexeditor.ad.fingerprints.PrimaryAdsFingerprint

@Patch(
    name = "Disable ads",
    compatiblePackages = [CompatiblePackage("com.myprog.hexedit")]
)
@Suppress("unused")
object DisableAdsPatch : BytecodePatch(
    setOf(PrimaryAdsFingerprint)
) {
    override fun execute(context: BytecodeContext) = PrimaryAdsFingerprint.result?.mutableMethod?.replaceInstructions(
        0,
        """
            const/4 v0, 0x1
            return v0
        """
    ) ?: throw PrimaryAdsFingerprint.exception
}
