package app.revanced.patches.tumblr.annoyances.tv

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tumblr.featureflags.addFeatureFlagOverride
import app.revanced.patches.tumblr.featureflags.overrideFeatureFlagsPatch

@Suppress("unused")
val disableTumblrTvPatch = bytecodePatch(
    name = "Disable Tumblr TV",
    description = "Removes the Tumblr TV navigation button from the bottom navigation bar.",
) {
    dependsOn(overrideFeatureFlagsPatch)

    compatibleWith("com.tumblr")

    execute {
        addFeatureFlagOverride("tumblrTvMobileNav", "false")
    }
}
