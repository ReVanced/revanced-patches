package app.revanced.patches.tumblr.annoyances.tv

import app.revanced.patcher.patch.creatingBytecodePatch
import app.revanced.patches.tumblr.featureflags.addFeatureFlagOverride
import app.revanced.patches.tumblr.featureflags.overrideFeatureFlagsPatch

@Suppress("unused")
val `Disable Tumblr TV` by creatingBytecodePatch(
    description = "Removes the Tumblr TV navigation button from the bottom navigation bar.",
) {
    dependsOn(overrideFeatureFlagsPatch)

    compatibleWith("com.tumblr")

    apply {
        addFeatureFlagOverride("tumblrTvMobileNav", "false")
    }
}
