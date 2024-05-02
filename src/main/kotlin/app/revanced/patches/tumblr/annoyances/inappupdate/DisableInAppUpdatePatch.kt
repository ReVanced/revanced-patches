package app.revanced.patches.tumblr.annoyances.inappupdate

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.tumblr.featureflags.addOverride
import app.revanced.patches.tumblr.featureflags.overrideFeatureFlagsPatch

@Suppress("unused")
val disableInAppUpdatePatch = bytecodePatch(
    name = "Disable in-app update",
    description = "Disables the in-app update check and update prompt.",
) {
    compatibleWith("com.tumblr"())

    dependsOn(overrideFeatureFlagsPatch)

    execute {
        addOverride("inAppUpdate", "false")
    }
}
