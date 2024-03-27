package app.revanced.patches.reddit.customclients.ads

import app.revanced.patcher.PatchClass
import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patches.reddit.customclients.ads.fingerprints.IsAdsEnabledFingerprint
import app.revanced.util.returnEarly

abstract class BaseDisableAdsPatch(
    dependencies: Set<PatchClass> = emptySet(),
    compatiblePackages: Set<CompatiblePackage>,
) : BytecodePatch(
    name = "Disable ads",
    dependencies = dependencies,
    compatiblePackages = compatiblePackages,
    fingerprints = setOf(IsAdsEnabledFingerprint),
) {
    override fun execute(context: BytecodeContext) = listOf(IsAdsEnabledFingerprint).returnEarly()
}
