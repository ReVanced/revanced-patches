package app.revanced.patches.tumblr.annoyances.adfree

import app.revanced.patcher.data.BytecodeContext
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.annotation.CompatiblePackage
import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patches.tumblr.featureflags.OverrideFeatureFlagsPatch

@Patch(
    name = "Disable Ad-Free Banner",
    description = "Disables the banner with a frog, prompting you to buy Tumblr Ad-Free.",
    dependencies = [OverrideFeatureFlagsPatch::class],
    compatiblePackages = [CompatiblePackage("com.tumblr")],
)
@Suppress("unused")
object DisableAdFreeBannerPatch : BytecodePatch(emptySet()) {
    override fun execute(context: BytecodeContext) {
        // Disable the "AD_FREE_CTA_BANNER" ("Whether or not to show ad free prompt") feature flag.
        OverrideFeatureFlagsPatch.addOverride("adFreeCtaBanner", "false")
    }
}
