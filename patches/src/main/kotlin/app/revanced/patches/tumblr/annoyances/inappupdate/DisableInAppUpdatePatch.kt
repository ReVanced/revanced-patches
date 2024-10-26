package app.revanced.patches.tumblr.annoyances.inappupdate

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tumblr.featureflags.addFeatureFlagOverride
import app.revanced.patches.tumblr.featureflags.overrideFeatureFlagsPatch

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disables the in-app update check and update prompt.",
) {
    dependsOn(overrideFeatureFlagsPatch)

    compatibleWith("com.tumblr")

    execute {
        // Before checking for updates using Google Play core AppUpdateManager, the value of this feature flag is checked.
        // If this flag is false or the last update check was today and no update check is performed.
        addFeatureFlagOverride("inAppUpdate", "false")
    }
}
