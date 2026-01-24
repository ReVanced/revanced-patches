package app.revanced.patches.tumblr.annoyances.adfree

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.tumblr.featureflags.addFeatureFlagOverride
import app.revanced.patches.tumblr.featureflags.overrideFeatureFlagsPatch

@Suppress("unused", "ObjectPropertyName")
val `Disable Ad-Free Banner` by creatingBytecodePatch(
    description = "Disables the banner with a frog, prompting you to buy Tumblr Ad-Free.",
) {
    dependsOn(overrideFeatureFlagsPatch)

    compatibleWith("com.tumblr")

    apply {
        // Disable the "AD_FREE_CTA_BANNER" ("Whether or not to show ad free prompt") feature flag.
        addFeatureFlagOverride("adFreeCtaBanner", "false")
    }
}
